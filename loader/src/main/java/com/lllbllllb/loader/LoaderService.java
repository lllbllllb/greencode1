package com.lllbllllb.loader;

import java.time.Duration;
import java.util.List;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoaderService {

    private final SessionService sessionService;

    private final AttemptService attemptService;

    private final LoadService loadService;

    private final List<Finalizable> finalizables;

    private final List<Resettable> resettables;

    private final List<Initializable> initializables;

    public void receiveEvent(String preyName, LoadConfiguration loadConfiguration) {
        loadService.registerLoadConfiguration(preyName, loadConfiguration);

        reset(preyName);

        var rps = loadConfiguration.rps();

        if (rps != 0) {
            var disposable = Flux.interval(Duration.ofNanos(1_000_000_000L / rps))
                .parallel().runOn(Schedulers.boundedElastic())
                .flatMap(i -> {
                    var number = i + 1;
                    var prey = sessionService.getPrey(preyName);

                    return loadService.getUnit(preyName)
                        .map(tuple2 -> {
                            var responseTime = tuple2.getT1();

                            if (prey.expectedResponseStatusCode() == tuple2.getT2()) {
                                return attemptService.registerSuccess(preyName, number, responseTime);
                            } else {
                                return attemptService.registerError(preyName, number, responseTime);
                            }
                        })
                        .onErrorResume(e -> {
                            if (ReadTimeoutException.class.equals(e.getClass()) || ReadTimeoutException.class.equals(e.getCause().getClass())) {
                                return Mono.just(attemptService.registerTimeout(preyName, number, prey.timeoutMs()));
                            } else {
                                log.error(e.getMessage(), e);

                                return Mono.error(e);
                            }
                        });
                }, false, 9_999_999, 1)
                .subscribe(event -> sessionService.publishOutcomeEvent(preyName, event, rps));

            loadService.registerActiveLoaderDisposable(preyName, disposable);
        } else {
            reset(preyName);
        }
    }

    public Flux<AttemptResult> getLoadEventStream(String preyName) {
        return sessionService.subscribeToAttemptResultStream(preyName);
    }

    public void reset(String preyName) {
        resettables.forEach(resettable -> resettable.reset(preyName));
    }

    public void registerPrey(Prey prey) {
        initializables.forEach(initializable -> initializable.initialize(prey));

        log.info("Initialization for [{}] was successfully completed", prey.name());
    }

    public void disconnectPrey(String preyName) {
        var stopWhenDisconnect = getLoadConfiguration().stopWhenDisconnect();
        var finalize = sessionService.handleUnsubscribeFromAttemptResultStream(preyName, stopWhenDisconnect);

        if (finalize) {
            finalizePrey(preyName);
        }
    }

    public void finalizePrey(String preyName) {
        finalizables.forEach(finalizable -> finalizable.finalize(preyName));

        log.info("Finalization for [{}] was successfully completed", preyName);
    }

    public List<Prey> getAllPreys() {
        return sessionService.getAllPreys();
    }

    public LoadConfiguration getLoadConfiguration() {
        return loadService.getLoadConfiguration();
    }
}

package com.lllbllllb.loader;

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

    private final LoadConfigurationService loadConfigurationService;

    private final CountdownService countdownService;

    private final List<Finalizable> finalizables;

    private final List<Resettable> resettables;

    private final List<Initializable> initializables;

    public void receiveEvent(String preyName, LoadOptions loadOptions) {
        loadConfigurationService.updateLoadConfiguration(preyName, loadOptions);
        resettables.forEach(resettable -> resettable.reset(preyName));
        loadConfigurationService.getLoadInterval(preyName)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(interval -> countdownService.runCountdown(
                preyName,
                loadOptions,
                countdownTick -> sessionService.publishACountdownTick(preyName, countdownTick),
                () -> resettables.forEach(resettable -> resettable.reset(preyName))
            ))
            .map(interval -> Flux.interval(interval)
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
                }, false, loadConfigurationService.getMaxConcurrency(preyName), 1)
                .subscribe(event -> sessionService.publishAttemptResult(preyName, event)))
            .subscribe(disposable -> loadService.registerActiveLoaderDisposable(preyName, disposable));
    }

    public Flux<AttemptResult> getLoadEventStream(String preyName) {
        return sessionService.subscribeToAttemptResultStream(preyName);
    }

    public Flux<CountdownTick> getTimerEventStream(String preyName) {
        return sessionService.subscribeToCountdownTickStream(preyName);
    }

    public void registerPrey(Prey prey) {
        initializables.forEach(initializable -> initializable.initialize(prey));

        log.info("Initialization for [{}] was successfully completed", prey.name());
    }

    public void disconnectPrey(String preyName) {
        var stopWhenDisconnect = getLoadConfiguration().stopWhenDisconnect();
        var sessionsLeft = sessionService.handleUnsubscribeFromAttemptResultStream(preyName);

        if (sessionsLeft < 1 && stopWhenDisconnect) {
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

    public LoadOptions getLoadConfiguration() {
        return loadConfigurationService.getLoadConfiguration();
    }
}

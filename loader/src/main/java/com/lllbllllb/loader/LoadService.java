package com.lllbllllb.loader;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.netty.internal.shaded.reactor.pool.PoolAcquireTimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadService {

    private final Map<String, Sinks.Many<LoadQuaintResult>> serviceNameToLoadEventSink = new ConcurrentHashMap<>();

    private final Map<String, Disposable> serviceNameToDisposable = new ConcurrentHashMap<>();

    private final Map<String, WebClient> preyNameToWebClientMap = new ConcurrentHashMap<>();

    private final Clock clock;

    private final ConfigurationProperties properties;

    private final WebClientFactory webClientFactory;

    public void receiveEvent(String serviceName, IncomeEvent event) {
        var rps = event.getRps();

        if (rps == 0) {
            var prev = serviceNameToDisposable.remove(serviceName);

            if (prev != null && !prev.isDisposed()) {
                prev.dispose();
            }
        } else {
            var prev = serviceNameToDisposable.remove(serviceName);

            if (prev != null && !prev.isDisposed()) {
                prev.dispose();
            }

            var loaderConfig = properties.getLoaderConfig();
            var threadCap = loaderConfig.getThreadCap();
            var queuedTaskCap = loaderConfig.getQueuedTaskCap();
            var schedulerName = String.format("loadServiceBoundedElastic-%s-%s", threadCap, queuedTaskCap);
            var disposable = Flux.interval(Duration.ofNanos(1_000_000_000L / rps))
                .parallel().runOn(Schedulers.newBoundedElastic(threadCap, queuedTaskCap, schedulerName))
                .flatMap(i -> {
                    var start = clock.millis();
                    var count = i + 1;

                    return preyNameToWebClientMap.get(serviceName).get()
                        .retrieve()
                        .toBodilessEntity()
                        .map(entities -> {
                            var end = clock.millis();

                            return new LoadQuaintResult(serviceName, start, end, LoadQuaintResult.Summary.SUCCESS, count);
                        })
                        .onErrorResume(e -> {
                            log.error(e.getMessage(), e);

                            var end = clock.millis();
                            var timeouts = Set.of(
                                TimeoutException.class,
                                io.netty.handler.timeout.TimeoutException.class,
                                ReadTimeoutException.class,
                                PoolAcquireTimeoutException.class,
                                reactor.pool.PoolAcquireTimeoutException.class
                            );
                            var summary = timeouts.contains(e.getClass()) || timeouts.contains(e.getCause().getClass())
                                ? LoadQuaintResult.Summary.TIMEOUT
                                : LoadQuaintResult.Summary.SERVER_ERROR;

                            return Mono.just(new LoadQuaintResult(serviceName, start, end, summary, count));
                        });
                })
                .subscribe(events -> publishOutcomeEvent(serviceName, events, rps));

            serviceNameToDisposable.put(serviceName, disposable);
        }
    }

    public Flux<LoadQuaintResult> getLoadEventStream(String serviceName) {
        var loadEventSink = serviceNameToLoadEventSink.computeIfAbsent(serviceName, sn -> Sinks.many().unicast().onBackpressureBuffer());

        log.info("Loader for [{}] was initialized", serviceName);

        return loadEventSink.asFlux();
    }

    public void finalize(String serviceName) {
        serviceNameToLoadEventSink.remove(serviceName).emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);

        var disposable = serviceNameToDisposable.remove(serviceName);

        if (disposable != null) {
            disposable.dispose();
        }

        log.info("Loader for [{}] was finalized successfully", serviceName);
    }

    public void registerPrey(Prey prey) {
        var preyName = prey.getName();
        var webClient = webClientFactory.create(prey.getPath(), preyName);

        preyNameToWebClientMap.put(preyName, webClient);
    }

    private void publishOutcomeEvent(String serviceName, LoadQuaintResult loadQuaintResult, int rps) {
        var sink = serviceNameToLoadEventSink.get(serviceName);
        var freq = 50;

        if (rps > freq && loadQuaintResult.getTotalCount() % (rps / freq) != 0) {
            return;
        }

        if (sink != null) {
            sink.emitNext(loadQuaintResult, (signalType, emitResult) -> {
                if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                    LockSupport.parkNanos(10);
                    return true;
                } else {
                    return Sinks.EmitFailureHandler.FAIL_FAST.onEmitFailure(signalType, emitResult);
                }
            });
        }
    }

}

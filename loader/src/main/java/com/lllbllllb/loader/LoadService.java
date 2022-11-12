package com.lllbllllb.loader;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadService {

    private final Map<String, Sinks.Many<AttemptReport>> serviceNameToLoadEventSink = new ConcurrentHashMap<>();

    private final Map<String, Disposable> serviceNameToDisposable = new ConcurrentHashMap<>();

    private final Map<String, RequestHeadersSpec<?>> preyNameToWebClientMap = new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> preyNameToSuccessCountMap = new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> preyNameToTimeoutCountMap = new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> preyNameToErrorCountMap = new ConcurrentHashMap<>();

    private final Map<String, Prey> preyNameToPreyMap = new ConcurrentHashMap<>();

    private final AtomicInteger sessions = new AtomicInteger(0);

    private volatile CurrentLoadParameters currentLoadParameters = new CurrentLoadParameters(0, false);

    private final WebClientFactory webClientFactory;

    public void receiveEvent(String preyName, IncomeEvent incomeEvent) {
        var rps = incomeEvent.getRps();
        currentLoadParameters = new CurrentLoadParameters(rps, incomeEvent.isStopWhenDisconnect());
        var prev = serviceNameToDisposable.remove(preyName);

        if (prev != null && !prev.isDisposed()) {
            prev.dispose();
        }

        if (rps != 0) {
            var disposable = Flux.interval(Duration.ofNanos(1_000_000_000L / rps))
                .parallel().runOn(Schedulers.boundedElastic())
                .flatMap(i -> {
                    var number = i + 1;
                    var prey = preyNameToPreyMap.get(preyName);

                    return preyNameToWebClientMap.get(preyName)
                        .exchangeToMono(clientResponse -> {
                            var result = prey.getExpectedResponseStatusCode() == clientResponse.rawStatusCode()
                                ? AttemptReport.AttemptResult.SUCCESS
                                : AttemptReport.AttemptResult.UNEXPECTED_STATUS;

                            return Mono.just(result);
                        })
                        .elapsed()
                        .map(tuple2 -> {
                            var successCount = preyNameToSuccessCountMap.get(preyName).incrementAndGet();
                            var timeoutCount = preyNameToTimeoutCountMap.get(preyName).get();
                            var errorCount = preyNameToErrorCountMap.get(preyName).get();

                            return new AttemptReport(preyName, tuple2.getT1(), tuple2.getT2(), number, successCount, timeoutCount, errorCount);
                        })
                        .onErrorResume(e -> {
                            var timeouts = Set.of(
                                TimeoutException.class,
                                io.netty.handler.timeout.TimeoutException.class,
                                ReadTimeoutException.class
                            );

                            if (timeouts.contains(e.getClass()) || timeouts.contains(e.getCause().getClass())) {
                                var successCount = preyNameToSuccessCountMap.get(preyName).get();
                                var timeoutCount = preyNameToTimeoutCountMap.get(preyName).incrementAndGet();
                                var errorCount = preyNameToErrorCountMap.get(preyName).get();

                                return Mono.just(new AttemptReport(preyName, prey.getTimeoutMs(), AttemptReport.AttemptResult.TIMEOUT, number, successCount, timeoutCount, errorCount));
                            } else {
                                log.error(e.getMessage(), e);

                                return Mono.error(e);
                            }
                        });
                }, false, 9_999_999, 1)
                .subscribe(event -> publishOutcomeEvent(preyName, event, rps));

            serviceNameToDisposable.put(preyName, disposable);
        }
    }

    public Flux<AttemptReport> getLoadEventStream(String serviceName) {
        var loadEventSink = serviceNameToLoadEventSink.computeIfAbsent(serviceName, sn -> Sinks.many().multicast().onBackpressureBuffer());

        log.info("Loader for [{}] was initialized", serviceName);

        sessions.incrementAndGet();

        return loadEventSink.asFlux();
    }

    public void finalize(String preyName) {
        if (currentLoadParameters.stopWhenDisconnect() && sessions.decrementAndGet() < 1) {
            var loadEventSink = serviceNameToLoadEventSink.remove(preyName);

            if (loadEventSink != null) {
                loadEventSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            }

            var disposable = serviceNameToDisposable.remove(preyName);

            if (disposable != null) {
                disposable.dispose();
            }

            currentLoadParameters = new CurrentLoadParameters(0, false);

            preyNameToSuccessCountMap.put(preyName, new AtomicLong(0));
            preyNameToTimeoutCountMap.put(preyName, new AtomicLong(0));
            preyNameToErrorCountMap.put(preyName, new AtomicLong(0));

            log.info("Loader for [{}] was finalized successfully", preyName);
        }
    }

    public void registerPrey(Prey prey) {
        var requestHeadersSpec = webClientFactory.create(prey);

        var preyName = prey.getName();
        preyNameToWebClientMap.put(preyName, requestHeadersSpec);
        preyNameToPreyMap.put(preyName, prey);
        preyNameToSuccessCountMap.put(preyName, new AtomicLong(0));
        preyNameToTimeoutCountMap.put(preyName, new AtomicLong(0));
        preyNameToErrorCountMap.put(preyName, new AtomicLong(0));
    }

    public List<Prey> getAllPreys() {
        return preyNameToPreyMap.values().stream()
            .sorted(Comparator.comparing(Prey::getName))
            .collect(Collectors.toList());
    }

    public void deletePrey(String preyName) {
        preyNameToWebClientMap.remove(preyName);
        preyNameToPreyMap.remove(preyName);
        preyNameToSuccessCountMap.remove(preyName);
        preyNameToTimeoutCountMap.remove(preyName);
        preyNameToErrorCountMap.remove(preyName);
    }

    public CurrentLoadParameters getCurrentRps() {
        return currentLoadParameters;
    }

    private void publishOutcomeEvent(String serviceName, AttemptReport attemptReport, int rps) {
        var sink = serviceNameToLoadEventSink.get(serviceName);
        var freq = 50;

        if (rps > freq && attemptReport.attemptNumber() % (rps / freq) != 0) {
            return;
        }

        if (sink != null) {
            sink.emitNext(attemptReport, (signalType, emitResult) -> {
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

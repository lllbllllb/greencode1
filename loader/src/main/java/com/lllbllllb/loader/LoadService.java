package com.lllbllllb.loader;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadService {

    private final Map<String, Sinks.Many<OutcomeEvent>> serviceNameToLoadEventSink = new ConcurrentHashMap<>();

    private final Map<String, Disposable> serviceNameToDisposable = new ConcurrentHashMap<>();

    private final Map<String, WebClient> serviceNameToWebClientMap;

    private final Clock clock;

    public void receiveEvent(String serviceName, IncomeEvent event) {
        var rps = event.getRps();

        if (rps == 0) {
            serviceNameToDisposable.get(serviceName).dispose();
        } else {
            var prev = serviceNameToDisposable.remove(serviceName);

            if (prev != null && !prev.isDisposed()) {
                prev.dispose();
            }

            var disposable = Flux.interval(Duration.ofNanos(1_000_000_000L / rps))
                .parallel().runOn(Schedulers.boundedElastic())
                .flatMap(i -> {
                    var start = clock.millis();

                    return serviceNameToWebClientMap.get(serviceName).get()
                        .retrieve()
                        .bodyToMono(List.class)
                        .map(entities -> {
                            var end = clock.millis();

                            return new OutcomeEvent(serviceName, start, end, true);
                        })
                        .onErrorResume(e -> {
                            var end = clock.millis();

                            return Mono.just(new OutcomeEvent(serviceName, start, end, false));
                        });
                })
//                .onErrorContinue((err, i) -> log.error("{} was skipped by error", i, err))
//                .buffer(Duration.ofMillis(100))
                .subscribe(events -> publishOutcomeEvent(serviceName, events));

            serviceNameToDisposable.put(serviceName, disposable);
        }
    }

    public Flux<OutcomeEvent> getLoadEventStream(String serviceName) {
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

    private void publishOutcomeEvent(String serviceName, OutcomeEvent outcomeEvent) {
        var sink = serviceNameToLoadEventSink.get(serviceName);
        if (sink != null) {
            sink.emitNext(outcomeEvent, (signalType, emitResult) -> {
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

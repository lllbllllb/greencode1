package com.lllbllllb.loader;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static reactor.util.concurrent.Queues.SMALL_BUFFER_SIZE;

@Slf4j
@Service
public class SessionService implements Initializable, Finalizable {

    private final Map<String, Sinks.Many<AttemptResult>> serviceNameToLoadEventSink = new ConcurrentHashMap<>();

    private final Map<String, Prey> preyNameToPreyMap = new ConcurrentHashMap<>();

    private final AtomicInteger sessions = new AtomicInteger(0);

    @Override
    public void initialize(Prey prey) {
        var preyName = prey.name();

        preyNameToPreyMap.put(preyName, prey);

        var sink = Sinks.many().multicast().<AttemptResult>onBackpressureBuffer(SMALL_BUFFER_SIZE, false);

        serviceNameToLoadEventSink.put(preyName, sink);

        log.info("Session for [{}] was initialized", prey);
    }

    public Flux<AttemptResult> subscribeToAttemptResultStream(String preyName) {
        var loadEventSink = serviceNameToLoadEventSink.get(preyName);

        if (loadEventSink == null) {
            throw new IllegalStateException("Sink for [%s] already closed".formatted(preyName));
        }

        sessions.incrementAndGet();

        var stream = loadEventSink.asFlux();

        log.info("Output stream [{}] got a subscriber", preyName);

        return stream;
    }

    public int handleUnsubscribeFromAttemptResultStream(String preyName) {
        var sessionsCount = sessions.decrementAndGet();

        log.info("Output stream [{}] lost a subscriber. [{}] session(s) left", preyName, sessionsCount);

        return sessionsCount;
    }

    public Prey getPrey(String preyName) {
        return preyNameToPreyMap.get(preyName);
    }

    public List<Prey> getAllPreys() {
        return preyNameToPreyMap.values().stream()
            .sorted(Comparator.comparing(Prey::name))
            .collect(Collectors.toList());
    }

    @Override
    public void finalize(String preyName) {
        var prey = preyNameToPreyMap.remove(preyName);

        if (prey != null) {
            var loadEventSink = serviceNameToLoadEventSink.remove(preyName);

            if (loadEventSink != null) {
                loadEventSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            } else {
                log.warn("Output stream for [{}] was already finalized", preyName);
            }
        } else {
            log.warn("Prey [{}] was already finalized", preyName);
        }
    }

//    public void publishOutcomeEvent(String preyName, AttemptResult attemptResult, int rps) {
    public void publishOutcomeEvent(String preyName, AttemptResult attemptResult) {
        var sink = serviceNameToLoadEventSink.get(preyName);

        if (sink == null) {
            throw new IllegalStateException("Sink for [%s] not found".formatted(preyName));
        }
//
//        var freq = 50;
//
//        if (rps > freq && attemptResult.attemptNumber() % (rps / freq) != 0) {
//            return;
//        }

        sink.emitNext(attemptResult, (signalType, emitResult) -> {
            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                LockSupport.parkNanos(10);
                return true;
            } else if (emitResult == Sinks.EmitResult.FAIL_TERMINATED || emitResult == Sinks.EmitResult.FAIL_OVERFLOW) {
                finalize(preyName);

                return Sinks.EmitFailureHandler.FAIL_FAST.onEmitFailure(signalType, emitResult);
            } else {
                return Sinks.EmitFailureHandler.FAIL_FAST.onEmitFailure(signalType, emitResult);
            }
        });
    }
}

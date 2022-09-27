package com.lllbllllb.greencode;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.SneakyThrows;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class StrongTask {

    private final AtomicInteger ati = new AtomicInteger();

    private volatile int num;

    @SneakyThrows
    public Mono<String> getNextBlocking() {
        return Mono.fromCallable(() -> {
                synchronized (this) {
                    Thread.sleep(1000);

                    return Integer.toString(++num);
                }
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> getNextNonBlocking() {
        return Mono.just("[" + ati.incrementAndGet() + "]")
            .delayElement(Duration.ofMillis(1000));
    }

}

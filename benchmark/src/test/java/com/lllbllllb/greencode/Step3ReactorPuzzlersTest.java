package com.lllbllllb.greencode;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

@ExtendWith(TimingExtension.class)
public class Step3ReactorPuzzlersTest {

    // assembly vs subscription

    @Test
    void shouldNotPrint() throws Exception {
        Flux.range(0, 10)
            .doOnNext(i -> System.out.println("#doOnNext - " + i));

        Thread.sleep(100);
    }

    @Test
    void shouldNotPrint1() throws Exception {
        Flux.range(0, 10)
            .delayElements(Duration.ofSeconds(1))
            .doOnNext(i -> System.out.println("#doOnNext - " + i))
            .subscribe();

        Thread.sleep(100);
    }

    @Test
    void shouldPrint() throws Exception {
        Flux.range(0, 10)
            .doOnNext(i -> System.out.println("#doOnNext - " + i))
            .subscribe();

        Thread.sleep(100);
    }


    // publishers

    @Test
    void coldPublisher() throws Exception {
        var source = Flux.fromIterable(List.of("blue", "green", "orange", "purple"))
            .map(String::toUpperCase);

        source.subscribe(d -> System.out.println("Subscriber 1: " + d));
        source.subscribe(d -> System.out.println("Subscriber 2: " + d));

        Thread.sleep(100);
    }

    @Test
    void hotPublisher() throws Exception {
        var hotSource = Sinks.unsafe().many().multicast().<String>directBestEffort();
        var hotFlux = hotSource.asFlux().map(String::toUpperCase);

        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: " + d));

        hotSource.emitNext("blue", FAIL_FAST);
        hotSource.tryEmitNext("green").orThrow();

        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: " + d));

        hotSource.emitNext("orange", FAIL_FAST);
        hotSource.emitNext("purple", FAIL_FAST);
        hotSource.emitComplete(FAIL_FAST);

        Thread.sleep(100);
    }

    @Test
    void should1Test() throws Exception {
        Mono.fromCallable(() -> {
                System.out.printf("#collable - %s | %s%n", 1, Thread.currentThread().getName());

                Thread.sleep(1000);

                return 1;
            })
            .doOnNext(i -> System.out.printf("#onNext_1 - %s | %s%n", i, Thread.currentThread().getName()))
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(i -> System.out.printf("#onNext_1- %s | %s%n", i, Thread.currentThread().getName()))
            .subscribe();

        Thread.sleep(4000);

    }

}

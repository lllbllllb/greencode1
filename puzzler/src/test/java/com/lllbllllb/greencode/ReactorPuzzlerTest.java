package com.lllbllllb.greencode;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

@Slf4j
public class ReactorPuzzlerTest {

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

    // publishers
    @Test
    void oneMoreWayToGettingHot() throws Exception {
        var obj = Flux.just(1, 2, 3, 4)
            .delayElements(Duration.ofSeconds(1))
            .share();

        obj.subscribe(x -> System.out.println("#1 - " + x));

        Thread.sleep(3000);

        obj.subscribe(x -> System.out.println("#2 - " + x));

        Thread.sleep(3000);
    }

    @Test
    void publishOnVsSubscribeOn() throws Exception {
        Mono.just(1)
            .doOnNext(i -> System.out.printf("#onNext_0 - %s | %s%n", i, Thread.currentThread().getName()))
            .flatMap(i1 -> Mono.fromCallable(() -> {
                        System.out.printf("#collable - %s | %s%n", 1, Thread.currentThread().getName());

                        Thread.sleep(1000);

                        return 1;
                    })
                    .doOnNext(i -> System.out.printf("#onNext_1 - %s | %s%n", i, Thread.currentThread().getName()))
//                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(i -> System.out.printf("#onNext_2 - %s | %s%n", i, Thread.currentThread().getName()))
            )
            .doOnNext(i -> System.out.printf("#onNext_3 - %s | %s%n", i, Thread.currentThread().getName()))
//            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        Thread.sleep(4000);
    }

    @Test
    void fluxOrder() {
        Flux.just(1)
            .doFirst(() -> System.out.println("#1-doFirst"))
            .doFirst(() -> System.out.println("#2-doFirst"))
            .doFirst(() -> System.out.println("#3-doFirst"))
            .doOnNext(__ -> System.out.println("#1-doOnNext"))
            .doOnNext(__ -> System.out.println("#2-doOnNext"))
            .doOnNext(__ -> System.out.println("#3-doOnNext"))
            .doFinally(__ -> System.out.println("#1-doFinally"))
            .doFinally(__ -> System.out.println("#2-doFinally"))
            .doFinally(__ -> System.out.println("#3-doFinally"))
            .subscribe();
    }

    @Test
    void showErrorBehaviour() {
        Flux.just(1, 2, 3, 4, 5, 6)
            .doOnNext(i -> {
                if (i == 4) {
                    throw new IllegalArgumentException("" + i);
                }
            })
            .subscribe(System.out::println);
    }

    @Test
    void onErrorResumeExample() {
        Flux.just(1, 2, 3, 4, 5, 6)
            .doOnNext(i -> {
                if (i == 4) {
                    throw new IllegalArgumentException("" + i);
                }
            })
            .onErrorResume(t -> {
                System.out.println("#onErrorResume_1");

                return Mono.empty();
            })
            .subscribe(System.out::println);
    }

    @Test
    void onErrorContinueHelp() {
        Flux.just(1, 2, 3, 4, 5, 6)
            .doOnNext(i -> {
                if (i == 4) {
                    throw new IllegalArgumentException("" + i);
                }
            })
            .onErrorContinue((t, p) -> System.out.println("ex " + p))
            .subscribe(System.out::println);
    }

    @Test
    void onErrorContinueBreakOtherErrorHandlers() {
        Flux.just(1, 2, 3, 4, 5, 6)
            .doOnNext(i -> {
                if (i == 4) {
                    throw new IllegalArgumentException("" + i);
                }
            })
            .doOnError(t -> System.out.println("#doOnError_1"))
            .onErrorResume(t -> {
                System.out.println("#onErrorResume_1");

                return Mono.empty();
            })
            .onErrorContinue((t, value) -> System.out.println("#onErrorContinue - " + value))
            .subscribe(System.out::println);
    }

    @Test
    void onErrorResumeFixed() {
        Flux.just(1, 2, 3, 4, 5, 6)
            .flatMap(i -> Mono.create(sink -> {
                    if (i == 4) {
                        sink.error(new IllegalArgumentException("" + i));
                    } else {
                        sink.success(i);
                    }
                })
                .onErrorResume(ex -> {
                    System.out.println("#onErrorResume_1 - " + i);

                    return Mono.empty();
                }))
            .subscribe(System.out::println);
    }

    @Test
    void runPublisherImmediate() {
        Mono.just(new EchoObject("#1"))
            .switchIfEmpty(Mono.defer(() -> Mono.just(new EchoObject("#2"))))
            .doOnNext(e -> System.out.println("#3"))
            .switchIfEmpty(Mono.defer(() -> Mono.just(new EchoObject("#4"))))
            .subscribe();
    }

    @Test
    void createFluxFromStream() {
        Flux.defer(() -> Flux.fromStream(Stream.of("a", "b", "c")))
            .repeat(1)
            .subscribe(System.out::println);
    }

    // context

    @Test
    void tryMdc() {

        Flux.just("1", "2", "3", "4")
            .doOnNext(i -> {
                MDC.put(i, i);
                System.out.printf("#doOnNext_0 | thread=%s | i=%s%n", Thread.currentThread().getName(), i);
            })
            .doOnNext(i -> System.out.printf("#doOnNext_1 | thread=%s | i=%s | MDC=%s%n", Thread.currentThread().getName(), i, MDC.get(i)))
            .publishOn(Schedulers.parallel())
            .doOnNext(i -> System.out.printf("#doOnNext_2 | thread=%s | i=%s | MDC=%s%n", Thread.currentThread().getName(), i, MDC.get(i)))
            .subscribe();
    }

    @Test
    void useContext() {
        var key = "message";
        var r = Mono
            .deferContextual(ctx -> Mono.just("Hello " + ctx.get(key)))
            .contextWrite(ctx -> ctx.put(key, "Reactor"))
            .flatMap(s -> Mono.deferContextual(ctx -> Mono.just(s + " " + ctx.get(key))))
            .contextWrite(ctx -> ctx.put(key, "World"));

        StepVerifier.create(r)
            .expectNext("Hello Reactor World")
            .verifyComplete();
    }


    // Note that sequential() is implicitly applied if you subscribe to the ParallelFlux
    // with a Subscriber but not when using the lambda-based variants of subscribe.
}

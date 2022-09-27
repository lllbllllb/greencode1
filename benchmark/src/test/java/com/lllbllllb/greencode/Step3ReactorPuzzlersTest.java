package com.lllbllllb.greencode;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(TimingExtension.class)
public class Step3ReactorPuzzlersTest {

    @Test
    void test() throws Exception {
        Thread.sleep(2000);
    }

    @Test
    void should1Test() {
        var seq = List.of("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x");
        var strongTask = new StrongTask();

        StepVerifier.create(
                Flux.fromIterable(seq)
                    .doOnNext(System.out::println)
                    .flatMap(r -> strongTask.getNextBlocking().map(i -> String.format("%s - %s", r, i)))
                    .doOnNext(System.out::println)
                    .flatMap(t -> strongTask.getNextNonBlocking().map(s -> String.format("%s - %s", t, s)))
                    .doOnNext(System.out::println)
            )
            .expectNextCount(seq.size())
            .verifyComplete();
    }

}

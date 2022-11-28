package com.lllbllllb.slowpoke;

import java.time.Duration;

import com.lllbllllb.common.Entity;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_10;
import static com.lllbllllb.common.Constants.SLOWPOKE_5;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @GetMapping(SLOWPOKE_0)
    public Mono<Entity> getEntity0() {
        return Mono.fromCallable(() -> getRandomValueEntity("0"))
            .subscribeOn(Schedulers.boundedElastic());
    }
    
    @GetMapping(SLOWPOKE_5)
    public Mono<Entity> getEntity5() {
        return Mono.fromCallable(() -> getRandomValueEntity("5"))
            .delayElement(Duration.ofMillis(5))
            .subscribeOn(Schedulers.boundedElastic());
    }
    
    @GetMapping(SLOWPOKE_10)
    public Mono<Entity> getEntity10() {
        return Mono.fromCallable(() -> getRandomValueEntity("10"))
            .delayElement(Duration.ofMillis(10))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/publishOn")
    public Mono<String> publishOn() {
        return Mono.fromCallable(this::blocking)
            .publishOn(Schedulers.boundedElastic());
    }

    @GetMapping("/subscribeOn")
    public Mono<String> subscribeOn() {
        return Mono.fromCallable(this::blocking)
            .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/noScheduler")
    public Mono<String> noScheduler() {
        return Mono.fromCallable(this::blocking);
    }

    private Entity getRandomValueEntity(String arg) {
        return new Entity(arg, RandomStringUtils.randomAlphabetic(1));
    }

    @SneakyThrows
    private String blocking() {
        var rand = RandomStringUtils.randomAlphabetic(4);

        Thread.sleep(200);

        return rand;
    }
}

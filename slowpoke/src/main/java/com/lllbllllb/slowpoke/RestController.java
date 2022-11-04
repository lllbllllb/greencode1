package com.lllbllllb.slowpoke;

import java.time.Duration;

import com.lllbllllb.common.Entity;
import org.apache.commons.lang3.RandomStringUtils;
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

    private Entity getRandomValueEntity(String arg) {
        return new Entity(arg, RandomStringUtils.randomAlphabetic(1));
    }

}

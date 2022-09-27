package com.lllbllllb.slowpoke;

import java.time.Duration;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_100;
import static com.lllbllllb.common.Constants.SLOWPOKE_1000;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @GetMapping(SLOWPOKE_0)
    public Mono<Map<String, String>> getByte0() {
        return Mono.just(getRandom("0"));
    }

    @GetMapping(SLOWPOKE_100)
    public Mono<Map<String, String>> getByte100() {
        return Mono.just(getRandom("100"))
            .delayElement(Duration.ofMillis(100));
    }

    @GetMapping(SLOWPOKE_1000)
    public Mono<Map<String, String>> getByte1000() {
        return Mono.just(getRandom("1000"))
            .delayElement(Duration.ofMillis(1000));
    }

    private Map<String, String> getRandom(String arg) {
        return Map.of(arg, RandomStringUtils.randomAlphabetic(1));
    }

}

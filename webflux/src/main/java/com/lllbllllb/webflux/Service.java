package com.lllbllllb.webflux;

import java.util.HashMap;
import java.util.Map;

import com.lllbllllb.common.DbEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_100;
import static com.lllbllllb.common.Constants.SLOWPOKE_1000;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {

    private final WebClient slowpokeClient;

    private final Repository repository;

    public Mono<Map<String, String>> getStringSingle() {
        return getStringStream()
            .reduce((m1, m2) -> {
                var res = new HashMap<String, String>();

                res.putAll(m1);
                res.putAll(m2);

                return res;
            });
    }

    public Flux<Map<String, String>> getStringStream() {
        var rand = RandomStringUtils.randomAlphabetic(4);

        return Flux.merge(
            getByPath(SLOWPOKE_1000),
            getByPath(SLOWPOKE_100),
            getByPath(SLOWPOKE_0),
            getByName(rand)
            );
    }

    private Mono<Map<String, String>> getByPath(String path) {
        return slowpokeClient
            .get()
            .uri(path)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {});
    }

    private Mono<Map<String, String>> getByName(String name) {
        return repository.findAllByName(name)
            .map(DbEntity::getValue)
            .reduce((b1, b2) -> String.join("-", b1, b2))
            .defaultIfEmpty("")
            .map(str -> Map.of("db", str));
    }

}

package com.lllbllllb.webflux;

import com.lllbllllb.common.Entity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_10;
import static com.lllbllllb.common.Constants.SLOWPOKE_5;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {

    private final WebClient slowpokeClient;

    private final Repository repository;

    public Flux<Entity> getStringStream() {
        var rand = RandomStringUtils.randomAlphabetic(4);

        return Flux.merge(
            getByPath(SLOWPOKE_5),
            getByPath(SLOWPOKE_10),
            getByPath(SLOWPOKE_0),
            getByName(rand)
        );
    }

    private Mono<Entity> getByPath(String path) {
        return slowpokeClient
            .get()
            .uri(path)
            .retrieve()
            .bodyToMono(Entity.class);
    }

    private Flux<Entity> getByName(String name) {
        return repository.findAllByName(name)
            .map(dbEntity -> new Entity(name, dbEntity.getValue()));
    }

}

package com.lllbllllb.webflux;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import com.lllbllllb.common.Entity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static com.lllbllllb.common.Constants.DB_ID;
import static com.lllbllllb.common.Constants.DB_NAME;
import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_0_NAME;
import static com.lllbllllb.common.Constants.SLOWPOKE_10;
import static com.lllbllllb.common.Constants.SLOWPOKE_10_NAME;
import static com.lllbllllb.common.Constants.SLOWPOKE_5;
import static com.lllbllllb.common.Constants.SLOWPOKE_5_NAME;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {

    private final WebClient slowpokeClient;

    private final Repository repository;

    private Map<String, Supplier<Flux<Entity>>> getDataSupplier;

    @PostConstruct
    public void init() {
        getDataSupplier = getDataSupplier();
    }

    public Flux<Entity> getStringStream(List<String> names) {
        return Flux.fromIterable(names)
            .concatMap(name -> getDataSupplier.get(name).get());
    }

    private Flux<Entity> getFromSlowpokeByPath(String path) {
        return slowpokeClient
            .get()
            .uri(path)
            .retrieve()
            .bodyToMono(Entity.class)
            .as(Flux::from)
            .share();
    }

    private Flux<Entity> getFromDbByRandomName() {
        var name = RandomStringUtils.randomAlphabetic(4);

        return repository.findAllByName(name)
            .map(dbEntity -> new Entity(name, dbEntity.getValue()));
    }

    private Flux<Entity> getFromDbByRandomId() {
        var id = RandomUtils.nextLong(0, 1_000_000);

        return repository.findById(id)
            .map(dbEntity -> new Entity("" + id, dbEntity.getValue()))
            .as(Flux::from);
    }


    private Map<String, Supplier<Flux<Entity>>> getDataSupplier() {
        return Map.of(
            SLOWPOKE_0_NAME, () -> getFromSlowpokeByPath(SLOWPOKE_0),
            SLOWPOKE_5_NAME, () -> getFromSlowpokeByPath(SLOWPOKE_5),
            SLOWPOKE_10_NAME, () -> getFromSlowpokeByPath(SLOWPOKE_10),
            DB_NAME, this::getFromDbByRandomName,
            DB_ID, this::getFromDbByRandomId
        );

    }

}

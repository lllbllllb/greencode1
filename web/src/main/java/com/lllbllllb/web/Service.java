package com.lllbllllb.web;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.lllbllllb.common.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.client.RestTemplate;

import static com.lllbllllb.common.Constants.DB_ID;
import static com.lllbllllb.common.Constants.DB_NAME;
import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_0_NAME;
import static com.lllbllllb.common.Constants.SLOWPOKE_10;
import static com.lllbllllb.common.Constants.SLOWPOKE_10_NAME;
import static com.lllbllllb.common.Constants.SLOWPOKE_5;
import static com.lllbllllb.common.Constants.SLOWPOKE_5_NAME;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {

    private final RestTemplate restTemplate;

    private final Repository repository;

    private final ConfigurationProperties properties;

    private Map<String, Supplier<List<Entity>>> getDataSupplier;

    @PostConstruct
    public void init() {
        getDataSupplier = getDataSupplier();
    }

    public List<Entity> getStringStream(List<String> names) {
        return names.stream()
            .flatMap(name -> getDataSupplier.get(name).get().stream())
            .collect(Collectors.toList());
    }

    private List<Entity> getFromSlowpokeByPath(String path) {
        var res = restTemplate.getForEntity(properties.getSlowpokeHost() + path, Entity.class)
            .getBody();

        if (res == null) {
            return List.of();
        }

        return List.of(res);
    }

    private List<Entity> getFromDbByRandomName() {
        var name = RandomStringUtils.randomAlphabetic(4);

        return repository.findAllByName(name).stream()
            .map(dbEntity -> new Entity(name, dbEntity.getValue()))
            .collect(Collectors.toList());
    }

    private List<Entity> getFromDbByRandomId() {
        var id = RandomUtils.nextLong(0, 1_000_000);

        return repository.findById(id)
            .map(dbEntity -> List.of(new Entity("" + id, dbEntity.getValue())))
            .orElseGet(List::of);
    }

    private Map<String, Supplier<List<Entity>>> getDataSupplier() {
        return Map.of(
            SLOWPOKE_0_NAME, () -> getFromSlowpokeByPath(SLOWPOKE_0),
            SLOWPOKE_5_NAME, () -> getFromSlowpokeByPath(SLOWPOKE_5),
            SLOWPOKE_10_NAME, () -> getFromSlowpokeByPath(SLOWPOKE_10),
            DB_NAME, this::getFromDbByRandomName,
            DB_ID, this::getFromDbByRandomId
        );

    }

}

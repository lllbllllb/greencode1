package com.lllbllllb.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.lllbllllb.common.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.client.RestTemplate;

import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_10;
import static com.lllbllllb.common.Constants.SLOWPOKE_5;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {

    private final RestTemplate restTemplate;

    private final Repository repository;

    private final ConfigurationProperties properties;

    public List<Entity> getStringStream() {
        var rand = RandomStringUtils.randomAlphabetic(4);
        var result = new ArrayList<Entity>();

        result.add(getFromSlowpokeByPath(SLOWPOKE_5));
        result.add(getFromSlowpokeByPath(SLOWPOKE_10));
        result.add(getFromSlowpokeByPath(SLOWPOKE_0));
        result.addAll(getFromDbByName(rand));

        return result;
    }

    private Entity getFromSlowpokeByPath(String path) {
        return restTemplate.getForEntity(properties.getSlowpokeHost() + path, Entity.class)
            .getBody();
    }

    private List<Entity> getFromDbByName(String name) {
        return repository.findAllByName(name).stream()
            .map(dbEntity -> new Entity(name, dbEntity.getValue()))
            .collect(Collectors.toList());
    }

}

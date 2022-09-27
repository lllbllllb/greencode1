package com.lllbllllb.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lllbllllb.common.DbEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.client.RestTemplate;

import static com.lllbllllb.common.Constants.SLOWPOKE_0;
import static com.lllbllllb.common.Constants.SLOWPOKE_100;
import static com.lllbllllb.common.Constants.SLOWPOKE_1000;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {

    private final RestTemplate restTemplate;

    private final Repository repository;

    private final ConfigurationProperties properties;

    public Map<String, String> getStringSingle() {
        var res = new HashMap<String, String>();

        getStringStream().forEach(res::putAll);

        return res;
    }

    public List<Map<String, String>> getStringStream() {
        var rand = RandomStringUtils.randomAlphabetic(4);

        return List.of(
            getByPath(SLOWPOKE_1000),
            getByPath(SLOWPOKE_100),
            getByPath(SLOWPOKE_0),
            getByName(rand)
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getByPath(String path) {
        return (Map<String, String>) restTemplate.getForEntity(properties.getSlowpokeHost() + path, Map.class)
            .getBody();
    }

    private Map<String, String> getByName(String name) {
        var val = repository.findAllByName(name).stream()
            .map(DbEntity::getValue)
            .reduce((s1, s2) -> String.join("-", s1, s2))
            .orElse("");

        return Map.of("db", val);
    }

}

package com.lllbllllb.loader;

import javax.annotation.PostConstruct;

import com.lllbllllb.common.DbEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@Profile("fill_db")
@RequiredArgsConstructor
public class FillDbService {

    private final Repository repository;

    @PostConstruct
    @Transactional
    public void fillDb() {
        Flux.range(0, 1_000_000)
            .map(id -> {
                var rand3 = RandomStringUtils.randomAlphabetic(4);
                var rand1 = RandomStringUtils.randomAlphanumeric(1);

                return new DbEntity(id.longValue(), rand3, rand1, null);
            })
            .as(repository::saveAll)
            .subscribe(entity -> log.info("saved {}", entity));
    }

}

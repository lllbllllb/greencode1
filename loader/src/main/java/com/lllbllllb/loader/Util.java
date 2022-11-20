package com.lllbllllb.loader;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.annotation.PostConstruct;

import com.lllbllllb.common.DbEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class Util {

    private final Repository repository;

    @PostConstruct
    @Transactional
    public void fillDb() {
        LongStream.range(0, 1_000)
            .mapToObj(i -> {
                    var from = i * 1000;
                    var to = from + 1000;

                    return LongStream.range(from, to)
                        .mapToObj(id -> {
                            var rand3 = RandomStringUtils.randomAlphabetic(4);
                            var rand1 = RandomStringUtils.randomAlphanumeric(1);

                            return new DbEntity(id, rand3, rand1, null);
                        })
                        .collect(Collectors.toList());
                }
            )
            .peek(list -> log.info("Batch to save size is {}", list.size()))
            .forEach(repository::saveAll);
    }

}

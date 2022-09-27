package com.lllbllllb.webflux;

import com.lllbllllb.common.DbEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface Repository extends ReactiveCrudRepository<DbEntity, Long> {

    Flux<DbEntity> findAllByName(String name);

}

package com.lllbllllb.web;

import java.util.List;

import com.lllbllllb.common.DbEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository.
 *
 * @author Yahor Pashkouski
 * @since 19.09.2022
 */
public interface Repository extends CrudRepository<DbEntity, Long> {

    List<DbEntity> findAllByName(String name);
}

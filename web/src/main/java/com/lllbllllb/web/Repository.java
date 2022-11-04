package com.lllbllllb.web;

import java.util.List;

import com.lllbllllb.common.DbEntity;
import org.springframework.data.repository.CrudRepository;

public interface Repository extends CrudRepository<DbEntity, Long> {

    List<DbEntity> findAllByName(String name);
}

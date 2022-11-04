package com.lllbllllb.loader;

import com.lllbllllb.common.DbEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * Repository.
 *
 * @author Yahor Pashkouski
 * @since 19.09.2022
 */
public interface Repository extends R2dbcRepository<DbEntity, Long> {

}

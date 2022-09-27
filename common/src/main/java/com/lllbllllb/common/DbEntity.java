package com.lllbllllb.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("db_entity")
public class DbEntity implements Persistable<Long> {

    @Id
    private Long id;

    private String name;

    private String value;

    @Version
    private Long version;

    @Override
    public boolean isNew() {
        return version == null;
    }

}

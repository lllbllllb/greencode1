create table conversion_config
(
    id              bigint primary key auto_increment not null, # or use [config_id] and [template_id] as composite id
    uniq_key        tinytext unique                   not null,
    name            text,
    description     text,
    status          tinytext,
    insert_ts       timestamp,
    update_ts       timestamp,
    currency_sku_id bigint
);

create table tag
(
    config_id bigint,
    tag       tinytext
);

create table conversion_config_segmentation_template
(
    config_id   bigint,
    template_id bigint,
    segment_id  bigint,
    insert_ts   timestamp,
    update_ts   timestamp
);

create table conversion_config_reward_formula_conversion
(
    id             bigint primary key auto_increment not null,
    config_id      bigint,
    template_id    bigint,
    reward_type    tinytext,
    reward_item_id bigint,
    currency_value double,
    insert_ts      timestamp,
    update_ts      timestamp
);

create table conversion_config_reward_range_conversion
(
    id                         bigint primary key auto_increment not null,
    config_id                  bigint,
    template_id                bigint,
    reward_type                tinytext,
    reward_item_id             bigint,
    mapping_id                 int,
    conversion_currency_amount int,
    converted_amount           int,
    insert_ts                  timestamp,
    update_ts                  timestamp
);

create table conversion_config_reward_rounding
(
    id        bigint primary key auto_increment not null,
    config_id bigint,
    amount    int,
    round     int,
    direction text,
    insert_ts timestamp,
    update_ts timestamp
);

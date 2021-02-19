create table user_header
(
    id      varchar(50)   not null,
    user_id varchar(50)   null,
    props   varchar(1000) null,
    type    varchar(150)  null,
    constraint user_header_pk
        primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4

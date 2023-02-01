SET SESSION innodb_lock_wait_timeout = 7200;
CREATE TABLE IF NOT EXISTS message_task
(
    id        varchar(50)  NOT NULL,
    type      varchar(50)  NOT NULL COMMENT '消息类型',
    event     varchar(255) NOT NULL COMMENT '通知事件类型',
    user_id   varchar(50)  NOT NULL COMMENT '接收人id',
    task_type varchar(64)  NOT NULL,
    webhook   varchar(255) COMMENT 'webhook地址',
    CONSTRAINT message_manage_pk
        PRIMARY KEY (id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

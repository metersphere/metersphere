SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS novice_statistics (
    id            varchar(50) NOT NULL COMMENT 'ID',
    user_id       varchar(64) NOT NULL COMMENT '用户id',
    guide_step    tinyint NOT NULL DEFAULT '0' COMMENT '新手引导完成的步骤',
    guide_num     int(10) NOT NULL DEFAULT '1' COMMENT '新手引导的次数',
    data_option   longtext DEFAULT NULL COMMENT 'data option (JSON format)',
    create_time   bigint(13)  NOT NULL COMMENT 'Create timestamp',
    update_time   bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (id) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
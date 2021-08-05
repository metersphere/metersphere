-- 接口定义增加全局运行环境选择
CREATE TABLE IF NOT EXISTS `api_definition_env`
(
    `id`        varchar(50) NOT NULL COMMENT 'ID',
    `user_id`   varchar(50) NOT NULL COMMENT 'user id',
    `env_id`    varchar(50) NULL COMMENT 'Api Environment id',
    create_time bigint(13)  NULL,
    update_time bigint(13)  NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;

-- load_test_report_result_part
CREATE TABLE `load_test_report_result_part`
(
    `report_id`      VARCHAR(50) NOT NULL,
    `report_key`     VARCHAR(64) NOT NULL,
    `resource_index` INT         NOT NULL,
    `report_value`   LONGTEXT,
    PRIMARY KEY `load_test_report_result_report_id_report_key_index` (`report_id`, `report_key`, `resource_index`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;

ALTER TABLE test_resource_pool
    ADD backend_listener TINYINT(1) DEFAULT 1;

CREATE TABLE `load_test_report_result_realtime`
(
    `report_id`      VARCHAR(50) NOT NULL,
    `report_key`     VARCHAR(64) NOT NULL,
    `resource_index` INT         NOT NULL,
    `sort`           INT         NOT NULL,
    `report_value`   TEXT,
    PRIMARY KEY `load_test_report_result_report_id_report_key_index` (`report_id`, `report_key`, `resource_index`, `sort`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;
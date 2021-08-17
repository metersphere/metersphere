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

ALTER TABLE api_definition
    ADD follow_people VARCHAR(100) NULL;

ALTER TABLE load_test
    ADD follow_people VARCHAR(100) NULL;

ALTER TABLE api_test_case
    ADD follow_people VARCHAR(100) NULL;

ALTER TABLE test_plan ADD report_summary TEXT NULL COMMENT '测试计划报告总结';

CREATE TABLE `plugin` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(300) DEFAULT NULL COMMENT 'plugin name',
  `plugin_id` varchar(300) NOT NULL COMMENT 'Plugin id',
  `clazz_name` varchar(500) NOT NULL COMMENT 'Plugin clazzName',
  `source_path` varchar(300) NOT NULL COMMENT 'Plugin jar path',
  `source_name` varchar(300) NOT NULL COMMENT 'Plugin jar name',
  `form_option` longtext COMMENT 'plugin form option',
  `form_script` longtext COMMENT 'plugin form script',
  `exec_entry` varchar(300) DEFAULT NULL COMMENT 'plugin init entry class',
  `create_time` bigint(13) DEFAULT NULL,
  `update_time` bigint(13) DEFAULT NULL,
  `create_user_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
)  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;
    
ALTER TABLE test_plan
    ADD report_summary TEXT NULL COMMENT '测试计划报告总结';

CREATE TABLE IF NOT EXISTS `notification`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `type`        VARCHAR(30)  DEFAULT NULL COMMENT '通知类型',
    `receiver`    VARCHAR(100) DEFAULT NULL COMMENT '接收人',
    `title`       VARCHAR(100) DEFAULT NULL COMMENT '标题',
    `content`     LONGTEXT COMMENT '内容',
    `status`      VARCHAR(30)  DEFAULT NULL COMMENT '状态',
    `create_time` BIGINT(13)   DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `IDX_RECEIVER` (`receiver`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;

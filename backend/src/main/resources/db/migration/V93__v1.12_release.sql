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

ALTER TABLE test_plan
    ADD report_summary TEXT NULL COMMENT '测试计划报告总结';

UPDATE message_task
SET user_id = 'CREATOR'
WHERE user_id = 'FOUNDER';

CREATE TABLE IF NOT EXISTS `notification`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `type`          varchar(30)  DEFAULT NULL COMMENT '通知类型',
    `receiver`      varchar(50)  DEFAULT NULL COMMENT '接收人',
    `title`         varchar(100) DEFAULT NULL COMMENT '标题',
    `content`       longtext COMMENT '内容',
    `status`        varchar(30)  DEFAULT NULL COMMENT '状态',
    `create_time`   bigint(13)   DEFAULT NULL COMMENT '更新时间',
    `operator`      varchar(50)  DEFAULT NULL COMMENT '操作人',
    `operation`     varchar(50)  DEFAULT NULL,
    `resource_id`   varchar(50)  DEFAULT NULL COMMENT '资源ID',
    `resource_type` varchar(50)  DEFAULT NULL,
    `resource_name` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `IDX_RECEIVER` (`receiver`) USING BTREE,
    KEY `IDX_RECEIVER_TYPE` (`receiver`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;

-- 项目表增加tcp相关的字段
ALTER TABLE `project`
    ADD `mock_tcp_port` int(11) NULL;
ALTER TABLE `project`
    ADD `is_mock_tcp_open` tinyint(1) NOT NULL DEFAULT 0;

-- 场景表增加执行次数字段
ALTER TABLE `api_scenario`
    ADD `execute_times` int(11) NULL;

ALTER TABLE `api_scenario_report`  ADD `end_time` bigint(13) ;

-- 修改文档分享表
ALTER TABLE api_document_share RENAME TO share_info;
ALTER TABLE share_info change
    column share_api_id custom_data longtextCHARACTER
    SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Share Custom Data';

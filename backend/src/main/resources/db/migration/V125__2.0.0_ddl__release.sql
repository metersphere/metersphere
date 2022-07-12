-- 初始化V2.0.0
-- V117_v1-20-3_custom_field
-- 存储用例的自定义字段值
CREATE TABLE IF NOT EXISTS custom_field_test_case
(
    resource_id varchar(50) NOT NULL,
    field_id    varchar(50) NOT NULL,
    value       varchar(500),
    text_value  text,
    PRIMARY KEY (resource_id, field_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 存储缺陷的自定义字段值
CREATE TABLE IF NOT EXISTS custom_field_issues
(
    resource_id varchar(50) NOT NULL,
    field_id    varchar(50) NOT NULL,
    value       varchar(500),
    text_value  text,
    PRIMARY KEY (resource_id, field_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 自定义字段是否为第三方平台字段
ALTER TABLE custom_field
    ADD third_part TINYINT(1) DEFAULT 0 NOT NULL;

-- 变更标题和描述字段长度
ALTER TABLE issues
    MODIFY title varchar(128) NULL;
ALTER TABLE issues
    MODIFY description longtext NULL;

--
-- V120__1-20-4_retry_related_fields
ALTER TABLE `api_execution_queue_detail`
    ADD retry_enable TINYINT(1) DEFAULT 0 COMMENT '是否开启失败重试';
ALTER TABLE `api_execution_queue_detail`
    ADD retry_number BIGINT(13) COMMENT '失败重试次数';
ALTER TABLE `api_definition_exec_result`
    ADD env_config LONGTEXT COMMENT '执行环境配置';
ALTER TABLE `api_scenario_report`
    ADD env_config LONGTEXT COMMENT '执行环境配置';


--
-- V2_workstation_sync
/** api_definition add to_be_updated*/
ALTER TABLE `api_definition`
    ADD to_be_updated TINYINT(1) COMMENT '是否需要同步';

/** api_test_case add to_be_updated*/
ALTER TABLE `api_test_case`
    ADD to_be_updated TINYINT(1) COMMENT '是否需要同步';

--
-- jianguo

CREATE TABLE IF NOT EXISTS `api_execution_info`
(
    `id`          varchar(50) NOT NULL,
    `source_id`   varchar(50) NOT NULL COMMENT 'api definition id',
    `result`      varchar(50) NOT NULL,
    `create_time` BIGINT(13)  NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `source_id` (`source_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `api_case_execution_info`
(
    `id`           varchar(50) NOT NULL,
    `source_id`    varchar(50) NOT NULL COMMENT 'api test case id',
    `result`       varchar(50) NOT NULL,
    `trigger_mode` varchar(50),
    `create_time`  BIGINT(13)  NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `source_id` (`source_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `scenario_execution_info`
(
    `id`           varchar(50) NOT NULL,
    `source_id`    varchar(50) NOT NULL COMMENT 'api scenario id',
    `result`       varchar(50) NOT NULL,
    `trigger_mode` varchar(50),
    `create_time`  BIGINT(13)  NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `source_id` (`source_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;



CREATE TABLE IF NOT EXISTS `function_case_execution_info`
(
    `id`          varchar(50) NOT NULL,
    `source_id`   varchar(50) NOT NULL COMMENT 'test case id',
    `result`      varchar(50) NOT NULL,
    `create_time` BIGINT(13)  NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `source_id` (`source_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;

--
-- V126_2-0-0_create_element_reference
CREATE TABLE IF NOT EXISTS `ui_element_reference`
(
    `id`                VARCHAR(50) NOT NULL COMMENT 'ID',
    `element_id`        VARCHAR(255)         DEFAULT '' COMMENT 'Element ID this reference belongs to',
    `element_module_id` VARCHAR(255)         DEFAULT '' COMMENT 'Element module ID this reference belongs to',
    `scenario_id`       VARCHAR(255)         DEFAULT '' COMMENT 'Scenario ID this reference belongs to',
    `project_id`        VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'Project ID this reference belongs to',
    `create_time`       BIGINT      NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='UI element reference relation';


CREATE TABLE IF NOT EXISTS `ui_task_refresh`
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `task_key`    VARCHAR(255) DEFAULT '' COMMENT 'Key',
    `task_status` INT          DEFAULT 0 COMMENT 'task status(0: todo, 1: complete)',
    `create_time` BIGINT      NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='UI refresh task';


ALTER TABLE `ui_element`
    ADD COLUMN `update_user` VARCHAR(100) DEFAULT '' NOT NULL COMMENT 'update user' AFTER `create_user`;


ALTER TABLE `ui_element_module`
    ADD COLUMN `module_path` VARCHAR(1000) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'module path' AFTER `name`;

ALTER TABLE `ui_scenario_module`
    ADD COLUMN `module_path` VARCHAR(1000) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'module path' AFTER `name`;


--
-- V122_2-0-0_test_case_add_last_execute_result
-- 功能用例添加测试计划对应的最近执行状态
ALTER TABLE test_case
    ADD last_execute_result varchar(25) DEFAULT 'Prepare' NULL COMMENT '最近一次的测试计划的执行结果';

--
-- V128_2-0-0_test_case_report_add_env
ALTER TABLE `test_plan_report`
    ADD COLUMN `run_info` LONGTEXT COMMENT 'request (JSON format)';

--
-- V122_2-0-0_create_attachment
CREATE TABLE IF NOT EXISTS `file_attachment_metadata`
(
    `id`          varchar(64)  NOT NULL COMMENT 'File ID',
    `name`        varchar(250) NOT NULL COMMENT 'File name',
    `type`        varchar(64)  NULL DEFAULT NULL COMMENT 'File type',
    `size`        bigint(13)   NOT NULL COMMENT 'File size',
    `create_time` bigint(13)   NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)   NOT NULL COMMENT 'Update timestamp',
    `creator`     varchar(50)  NOT NULL COMMENT 'creator',
    `file_path`   varchar(250) NOT NULL COMMENT 'File Upload Path',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `file_name` (`name`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `issue_file`
(
    `issue_id` varchar(64) NOT NULL COMMENT 'ISSUE ID',
    `file_id`  varchar(64) NOT NULL COMMENT 'File ID',
    PRIMARY KEY `issue_file_unique_key` (`issue_id`, `file_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;

--
-- V129_2-0-0_test_case_report_api_base_count
ALTER TABLE `test_plan_report_content`
    ADD COLUMN `api_base_count` LONGTEXT COMMENT 'request (JSON format)';
SET SESSION innodb_lock_wait_timeout = 7200;

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
    `id`           varchar(50)  NOT NULL,
    `source_id`    varchar(255) NOT NULL COMMENT 'api scenario id',
    `result`       varchar(50)  NOT NULL,
    `trigger_mode` varchar(50),
    `create_time`  BIGINT(13)   NOT NULL COMMENT 'Create timestamp',
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

-- V129_2-0-0_test_case_report_api_base_count
SELECT IF(EXISTS(SELECT DISTINCT COLUMN_NAME
                 FROM information_schema.columns
                 WHERE table_schema = DATABASE()
                   AND table_name = 'test_plan_report_content'
                   AND COLUMN_NAME = 'api_base_count'),
          'select 1',
          'ALTER TABLE `test_plan_report_content` ADD COLUMN `api_base_count` LONGTEXT')
INTO @add_api_base_count;
PREPARE stmt_add_api_base_count FROM @add_api_base_count;
EXECUTE stmt_add_api_base_count;
DEALLOCATE PREPARE stmt_add_api_base_count;

--
-- V1_2-0-0_load_test_remember_environment
ALTER TABLE `load_test`
    ADD COLUMN `env_info` LONGTEXT COMMENT 'environment (JSON format)';
ALTER TABLE `load_test_report`
    ADD COLUMN `env_info` LONGTEXT COMMENT 'environment (JSON format)';


--
-- V125_2-0-0_test_plan_ui_scenario
CREATE TABLE `test_plan_ui_scenario`
(
    `id`                   VARCHAR(50) NOT NULL COMMENT 'ID',
    `test_plan_id`         VARCHAR(50) NOT NULL COMMENT 'Test plan ID',
    `ui_scenario_id`       VARCHAR(255) DEFAULT NULL,
    `status`               VARCHAR(50)  DEFAULT NULL COMMENT 'Scenario case status',
    `environment`          LONGTEXT COMMENT 'Relevance environment',
    `create_time`          BIGINT(20)  NOT NULL COMMENT 'Create timestamp',
    `update_time`          BIGINT(20)  NOT NULL COMMENT 'Update timestamp',
    `pass_rate`            VARCHAR(100) DEFAULT NULL,
    `last_result`          VARCHAR(100) DEFAULT NULL,
    `report_id`            VARCHAR(50)  DEFAULT NULL,
    `create_user`          VARCHAR(100) DEFAULT NULL,
    `order`                BIGINT(20)  NOT NULL COMMENT '排序，默认值5000',
    `environment_type`     VARCHAR(20)  DEFAULT NULL,
    `environment_group_id` VARCHAR(50)  DEFAULT NULL COMMENT 'ID',
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

ALTER TABLE test_plan_report_content
    ADD COLUMN plan_ui_scenario_report_struct LONGTEXT;

ALTER TABLE test_plan_report_content
    ADD COLUMN ui_result text COMMENT 'ui_result (JSON format)';


--
-- V125_2-0-0_test_case_comment
-- 测试计划用例状态去掉进行中
-- 评论添加类型，并初始化
ALTER TABLE test_case_comment
    ADD `type` varchar(20) DEFAULT ' ';

--
-- v2_api_add_to_update_time
ALTER TABLE `api_definition`
    ADD to_be_update_Time bigint(13) DEFAULT NULL COMMENT '需要同步的开始时间';

--
-- v2_api_case_add_to_update_time
ALTER TABLE `api_test_case`
    ADD to_be_update_Time bigint(13) DEFAULT NULL COMMENT '需要同步的开始时间';

--
-- 新增附件关系表
-- v2_init_attachment_module_relation
CREATE TABLE IF NOT EXISTS `attachment_module_relation`
(
    `relation_id`   varchar(64) NOT NULL COMMENT 'RELATION ID',
    `relation_type` varchar(64) NOT NULL COMMENT 'RELATION TYPE',
    `attachment_id` varchar(64) NOT NULL COMMENT 'ATTACHMENT ID',
    INDEX `attachment_module_index` (`relation_id`, `relation_type`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;

ALTER TABLE test_plan_report_content
    ADD COLUMN `ui_all_cases` LONGTEXT COMMENT 'ui all cases (JSON format)';


--
-- V2_sync_supplementary_data
-- 查出所有不重复的项目ID 循环项目ID 给 project_application 表设置默认值
DROP PROCEDURE IF EXISTS project_api_appl;

DELIMITER //
CREATE PROCEDURE project_api_appl()
BEGIN
    #声明结束标识
    DECLARE end_flag int DEFAULT 0;

    DECLARE projectId varchar(64);

    #声明游标 group_curosr
    DECLARE project_curosr CURSOR FOR SELECT DISTINCT id FROM project;

#设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET end_flag = 1;

    #打开游标
    OPEN project_curosr;
    #获取当前游标指针记录，取出值赋给自定义的变量
    FETCH project_curosr INTO projectId;
    #遍历游标
    REPEAT
        #利用取到的值进行数据库的操作
        DELETE FROM project_application WHERE project_id = projectId AND type = 'TRIGGER_UPDATE';
        INSERT INTO project_application (project_id, type, type_value)
        VALUES (projectId, 'TRIGGER_UPDATE',
                '{"protocol":true,"method":true,"path":true,"headers":true,"query":true,"rest":true,"body":true,"delNotSame":true,"runError":true,"unRun":true,"ids":null}');
        # 将游标中的值再赋值给变量，供下次循环使用
        FETCH project_curosr INTO projectId;
    UNTIL end_flag END REPEAT;

    #关闭游标
    CLOSE project_curosr;

END
//
DELIMITER ;

CALL project_api_appl();
DROP PROCEDURE IF EXISTS project_api_appl;

--
-- jianguo

DROP PROCEDURE IF EXISTS init_api_execution;

DELIMITER //
CREATE PROCEDURE init_api_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(64);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);

    DECLARE reports CURSOR FOR
        SELECT api_definition_exec_result.resource_id,
               api_definition_exec_result.`status`,
               api_definition_exec_result.create_time
        FROM api_definition_exec_result
                 INNER JOIN api_definition ON api_definition_exec_result.resource_id = api_definition.id
        WHERE api_definition_exec_result.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_definition_exec_result.create_time IS NOT NULL;

    #设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN reports;
    outer_loop:
    LOOP
        #获取当前游标指针记录，取出值赋给自定义的变量
        FETCH reports INTO id, `status`, create_time;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO api_execution_info (id, source_id, create_time, result)
        VALUES (UUID(), `id`, create_time, `status`);
    END LOOP;
    #关闭游标
    CLOSE reports;
END
//
DELIMITER ;
CALL init_api_execution();
DROP PROCEDURE IF EXISTS init_api_execution;


DROP PROCEDURE IF EXISTS init_scenario_execution;
DELIMITER //
CREATE PROCEDURE init_scenario_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(255);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);
    DECLARE `trigger_mode` varchar(50);
    DECLARE scenario_report CURSOR FOR
        SELECT DISTINCT api_scenario_report.scenario_id,
                        api_scenario_report.`status`,
                        api_scenario_report.create_time,
                        api_scenario_report.trigger_mode
        FROM api_scenario_report
                 INNER JOIN api_scenario ON api_scenario_report.scenario_id = api_scenario.id
        WHERE api_scenario_report.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_scenario_report.`status` IS NOT NULL
          AND api_scenario_report.`trigger_mode` IS NOT NULL;

    #设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN scenario_report;
    outer_loop:
    LOOP
        #获取当前游标指针记录取出值赋给自定义的变量
        FETCH scenario_report INTO id, `status`, create_time,`trigger_mode`;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO scenario_execution_info (id, source_id, create_time, result, trigger_mode)
        VALUES (UUID(), `id`, create_time, `status`, `trigger_mode`);
    END LOOP;
    #关闭游标
    CLOSE scenario_report;
END
//
DELIMITER ;

CALL init_scenario_execution();
DROP PROCEDURE IF EXISTS init_scenario_execution;


DROP PROCEDURE IF EXISTS init_api_case_execution;
DELIMITER //
CREATE PROCEDURE init_api_case_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(64);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);
    DECLARE `trigger_mode` varchar(50);

    DECLARE caseReports CURSOR FOR
        SELECT api_definition_exec_result.resource_id,
               api_definition_exec_result.`status`,
               api_definition_exec_result.create_time,
               api_definition_exec_result.trigger_mode
        FROM api_definition_exec_result
                 INNER JOIN api_test_case ON api_definition_exec_result.resource_id = api_test_case.id
        WHERE api_definition_exec_result.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_definition_exec_result.create_time IS NOT NULL
          AND api_definition_exec_result.trigger_mode IS NOT NULL;
    #设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN caseReports;
    outer_loop:
    LOOP
        #获取当前游标指针记录，取出值赋给自定义的变量
        FETCH caseReports INTO id, `status`, create_time,`trigger_mode`;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO api_case_execution_info (id, source_id, create_time, result, trigger_mode)
        VALUES (UUID(), `id`, create_time, `status`, `trigger_mode`);
    END LOOP;
    #关闭游标
    CLOSE caseReports;

END
//
DELIMITER ;
CALL init_api_case_execution();
DROP PROCEDURE IF EXISTS init_api_case_execution;



DROP PROCEDURE IF EXISTS init_plan_case_execution;
DELIMITER //
CREATE PROCEDURE init_plan_case_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(64);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);
    DECLARE `trigger_mode` varchar(50);

    DECLARE caseReports CURSOR FOR
        SELECT api_definition_exec_result.resource_id,
               api_definition_exec_result.`status`,
               api_definition_exec_result.create_time,
               api_definition_exec_result.trigger_mode
        FROM api_definition_exec_result
                 INNER JOIN test_plan_api_case ON api_definition_exec_result.resource_id = test_plan_api_case.id
                 INNER JOIN api_test_case ON test_plan_api_case.api_case_id = api_test_case.id
        WHERE api_definition_exec_result.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_definition_exec_result.create_time IS NOT NULL
          AND api_definition_exec_result.trigger_mode IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN caseReports;
    outer_loop:
    LOOP
        #获取当前游标指针记录，取出值赋给自定义的变量
        FETCH caseReports INTO id, `status`, create_time,`trigger_mode`;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO api_case_execution_info (id, source_id, create_time, result, trigger_mode)
        VALUES (UUID(), `id`, create_time, `status`, `trigger_mode`);
    END LOOP;
    #关闭游标
    CLOSE caseReports;

END
//
DELIMITER ;
CALL init_plan_case_execution();
DROP PROCEDURE IF EXISTS init_plan_case_execution;

-- 初始化 last_execute_result
UPDATE test_case, test_plan_test_case
SET test_case.last_execute_result = test_plan_test_case.status
WHERE test_case.id = test_plan_test_case.case_id;


--
-- V125_2-0-0_test_case_comment
UPDATE test_plan_test_case
SET status = 'Prepare'
WHERE status = 'Underway';

UPDATE test_case_comment
SET `type` = 'REVIEW'
WHERE status IN ('UnPass', 'Pass');

UPDATE test_case_comment
SET `type` = 'PLAN'
WHERE `type` = '';

--
-- 初始化attachment_module_relation数据
INSERT INTO attachment_module_relation
SELECT case_id, 'testcase', file_id
FROM test_case_file;
-- 清空test_case_file表数据
DELETE
FROM test_case_file;


--
-- v2_init_permissions
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_LINK_DEMAND', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_LINK_DEMAND', 'PROJECT_TRACK_CASE');

--
-- V127__2-0-1_add_test_plan_ui_fail_cases
ALTER TABLE test_plan_report_content
    ADD COLUMN `ui_failure_cases` LONGTEXT COMMENT 'ui failure cases (JSON format)';

--
-- V128__2-0-1_update_api_scenario_last_result
UPDATE api_scenario
set last_result=''
where last_result IS NULL;

SET SESSION innodb_lock_wait_timeout = DEFAULT;

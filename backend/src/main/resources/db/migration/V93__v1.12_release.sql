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

ALTER TABLE `api_scenario_report`
    ADD `end_time` bigint(13);

-- 修改文档分享表
ALTER TABLE api_document_share RENAME TO share_info;
ALTER TABLE share_info
    CHANGE
        COLUMN share_api_id custom_data longtext CHARACTER
        SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Share Custom Data';

ALTER TABLE test_plan
    ADD report_config text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '测试计划报告配置';

-- 删除不用的记录表
DROP TABLE test_plan_report_resource;
-- 统一接口案例未执行的状态字段
UPDATE api_test_case
SET `status` = ''
WHERE `status` = 'Underway';
UPDATE api_test_case
SET `original_status` = ''
WHERE `original_status` = 'Underway';

-- 设置默认的通知

DROP PROCEDURE IF EXISTS set_notice;
DELIMITER //
CREATE PROCEDURE set_notice()
BEGIN
    DECLARE orgId VARCHAR(64);
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT id
                                FROM organization);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO orgId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        --


        INSERT INTO message_task (id, type, event, user_id, task_type, webhook, identification, is_set, organization_id,
                                  test_id, create_time, template)
        VALUES (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'PERFORMANCE_TEST_TASK', '',
                'ce692111-166b-491a-ae69-f047c31de971', 0,
                orgId, NULL, 1629686472659, NULL),
               (UUID(), 'IN_SITE', 'COMPLETE', 'CREATOR', 'REVIEW_TASK',
                '',
                '72836b2d-4c2f-4185-95aa-1894c6f0d1c3', 0, orgId, NULL, 1629697096803,
                NULL),
               (UUID(), 'IN_SITE', 'CLOSE_SCHEDULE', 'CREATOR', 'TRACK_HOME_TASK', '',
                'ceb0aeb5-f194-4183-a995-3607a769c61d',
                0, orgId, NULL, 1629705930753, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'API_AUTOMATION_TASK', '',
                'e2865464-0da3-42bc-a041-7d21bd70d339', 0,
                orgId, NULL, 1629446356866, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'FOLLOW_PEOPLE', 'PERFORMANCE_TEST_TASK', '',
                '79d7dcdc-68a2-47a2-9ef5-21fdf9bde65c', 0, orgId, NULL, 1629446379928,
                NULL),
               (UUID(), 'IN_SITE', 'CLOSE_SCHEDULE', 'CREATOR', 'API_HOME_TASK', '',
                'dbe5dfcd-927f-4065-93cf-22f33d9570ac', 0,
                orgId, NULL, 1629446330438, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'FOLLOW_PEOPLE', 'TRACK_TEST_CASE_TASK', '',
                '3a632784-c73b-4f5f-824c-bdf4fccf6f4d', 0, orgId, NULL, 1629705939795,
                NULL),
               (UUID(), 'IN_SITE', 'CASE_UPDATE', 'FOLLOW_PEOPLE', 'API_DEFINITION_TASK', '',
                'a6a3979a-bd80-414c-a253-06f0364c434f', 0, orgId, NULL, 1629446346104,
                NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'FOLLOW_PEOPLE', 'API_AUTOMATION_TASK', '',
                '019de091-1a19-4b30-bd5e-23b83bc820fb',
                0, orgId, NULL, 1629618010742, NULL),
               (UUID(), 'IN_SITE', 'CASE_UPDATE', 'CREATOR', 'API_DEFINITION_TASK', '',
                'a6a3979a-bd80-414c-a253-06f0364c434f',
                0, orgId, NULL, 1629446346104, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'DEFECT_TASK', '', '66568b56-4f9e-4bf6-8621-7402403368b9', 0,
                orgId, NULL, 1629446306675, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'FOLLOW_PEOPLE', 'PERFORMANCE_TEST_TASK', '',
                'ce692111-166b-491a-ae69-f047c31de971', 0, orgId, NULL, 1629686472659,
                NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'CREATOR', 'DEFECT_TASK', '', '3a913f72-7cee-4e36-ae1d-0443c7cb5f97', 0,
                orgId, NULL, 1629699608062, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'FOLLOW_PEOPLE', 'API_DEFINITION_TASK', '',
                '7ca324cd-7113-412b-8714-5cbe1bffa535',
                0, orgId, NULL, 1629706088088, NULL),
               (UUID(), 'IN_SITE', 'CASE_DELETE', 'CREATOR', 'API_DEFINITION_TASK', '',
                'aeb9d21a-855c-44d3-a715-dfb1bba8ad6c',
                0, orgId, NULL, 1629446334332, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'API_REPORT_TASK', '', '4d3309f3-7ab2-492e-b9c9-1127c3e005d1',
                0,
                orgId, NULL, 1629446364391, NULL),
               (UUID(), 'IN_SITE', 'COMMENT', 'CREATOR', 'TRACK_TEST_CASE_TASK', '',
                '2e8db54f-2d39-42a6-9832-43bb3384e7d5', 0,
                orgId, NULL, 1629446247833, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'API_DEFINITION_TASK', '',
                '90f2be5d-0a71-44f0-a750-f16bf39d690b', 0,
                orgId, NULL, 1629446338557, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'PERFORMANCE_REPORT_TASK', '',
                '4c1bfa4a-b02b-4e8e-bc09-b13613723a11',
                0, orgId, NULL, 1629446384995, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'CREATOR', 'API_AUTOMATION_TASK', '',
                '019de091-1a19-4b30-bd5e-23b83bc820fb', 0,
                orgId, NULL, 1629618010742, NULL),
               (UUID(), 'IN_SITE', 'COMPLETE', 'CREATOR', 'TEST_PLAN_TASK', '', '86b42beb-a86f-4ff3-a73c-d25112c2f104',
                0,
                orgId, NULL, 1629446286106, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'CREATOR', 'PERFORMANCE_TEST_TASK', '',
                '79d7dcdc-68a2-47a2-9ef5-21fdf9bde65c', 0,
                orgId, NULL, 1629446379928, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'CREATOR', 'TRACK_TEST_CASE_TASK', '',
                '3a632784-c73b-4f5f-824c-bdf4fccf6f4d', 0,
                orgId, NULL, 1629705939795, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'CREATOR', 'API_DEFINITION_TASK', '',
                '7ca324cd-7113-412b-8714-5cbe1bffa535', 0,
                orgId, NULL, 1629706088088, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'TEST_PLAN_TASK', '', '04ea4fbd-a392-4f80-a61c-51d31a302cac', 0,
                orgId, NULL, 1629446289522, NULL),
               (UUID(), 'IN_SITE', 'UPDATE', 'CREATOR', 'REVIEW_TASK', '', 'db515209-f864-46d3-a2e5-63db6d1339c3', 0,
                orgId, NULL, 1629446276516, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'TRACK_REPORT_TASK', '', 'ea2fee85-8a44-413a-a128-16bfa01ada0d',
                0,
                orgId, NULL, 1629283758941, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'REVIEW_TASK', '', 'f9b1f60b-6dee-48af-8217-0428b27dcbab', 0,
                orgId, NULL, 1629446272477, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'CREATOR', 'TRACK_TEST_CASE_TASK', '',
                '7c26254e-85e9-4269-be13-a2ffcfe0b9f5', 0,
                orgId, NULL, 1629446251939, NULL),
               (UUID(), 'IN_SITE', 'DELETE', 'FOLLOW_PEOPLE', 'TRACK_TEST_CASE_TASK', '',
                '7c26254e-85e9-4269-be13-a2ffcfe0b9f5', 0, orgId, NULL, 1629446251939,
                NULL),
               (UUID(), 'IN_SITE', 'EXECUTE_SUCCESSFUL', 'CREATOR', 'API_DEFINITION_TASK', '',
                '7efddbdb-2b6c-4425-96a4-0bc2aa9e2cd2', 0, orgId, NULL, 1629775183917,
                NULL),
               (UUID(), 'IN_SITE', 'EXECUTE_FAILED', 'CREATOR', 'API_DEFINITION_TASK', '',
                'e508035c-1318-40ea-9457-0bbe9865f4ce', 0, orgId, NULL, 1629775194857,
                NULL),
               (UUID(), 'IN_SITE', 'EXECUTE_COMPLETED', 'CREATOR', 'PERFORMANCE_TEST_TASK', '',
                'e3db90c6-fb49-4e89-bc25-1d14b5ce94d0', 0, orgId, NULL, 1629790602744,
                NULL),
               (UUID(), 'IN_SITE', 'EXECUTE_FAILED', 'CREATOR', 'API_AUTOMATION_TASK', '',
                'c25930e8-b617-45f7-af5e-cc94adc14192', 0, orgId, NULL, 1629780485724,
                NULL),
               (UUID(), 'IN_SITE', 'EXECUTE_SUCCESSFUL', 'CREATOR', 'API_AUTOMATION_TASK', '',
                '9f91e5e5-1744-4160-bfc6-3851bfd59e05', 0, orgId, NULL, 1629780475764,
                NULL),
               (uuid(), 'IN_SITE', 'UPDATE', 'PROCESSOR', 'DEFECT_TASK', '',
                '6cad944e-db8d-4786-9ef3-7d6370940325', 0, orgId, NULL, 1629791388405,
                NULL),
               (uuid(), 'IN_SITE', 'CREATE', 'PROCESSOR', 'DEFECT_TASK', '',
                '4a890e41-e755-44fc-b734-d6a0ca25a65c', 0, orgId, NULL, 1629790487682,
                NULL);

        --
        SET done = 0;
    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL set_notice();
DROP PROCEDURE IF EXISTS set_notice;


-- 保存测试报告
ALTER TABLE test_plan_report
    ADD is_new TINYINT(1) NULL COMMENT 'v1.12报告改版标记';
ALTER TABLE test_plan_report
    MODIFY `is_api_case_executing` TINYINT(1) NULL COMMENT 'is Api Case executing';
ALTER TABLE test_plan_report
    MODIFY `is_scenario_executing` TINYINT(1) NULL COMMENT 'is scenario Case executing';
ALTER TABLE test_plan_report
    MODIFY `is_performance_executing` TINYINT(1) NULL COMMENT 'is performance executing';
CREATE TABLE IF NOT EXISTS `test_plan_report_content`
(
    `id`                     VARCHAR(50) NOT NULL COMMENT 'ID',
    `test_plan_report_id`    VARCHAR(50) NOT NULL COMMENT 'Test plan ID',
    `start_time`             bigint(13)  NULL,
    `case_count`             bigint(10)  NULL,
    `end_time`               bigint(13)  NULL,
    `execute_rate`           DOUBLE      NULL,
    `pass_rate`              DOUBLE      NULL,
    `is_third_part_issue`    TINYINT(1)  NULL COMMENT 'is third part issue',

    `config`                 text COMMENT 'plan config (JSON format)',
    `summary`                text COMMENT 'summary',

    `function_result`        text COMMENT 'function result (JSON format)',
    `api_result`             text COMMENT 'api result (JSON format)',
    `load_result`            text COMMENT 'api result (JSON format)',

    `function_all_cases`     longtext COMMENT 'function all cases (JSON format)',
    `function_failure_cases` longtext COMMENT 'function failure cases (JSON format)',
    `issue_list`             longtext COMMENT 'issue list (JSON format)',

    `api_all_cases`          longtext COMMENT 'api all cases (JSON format)',
    `api_failure_cases`      longtext COMMENT 'api failure cases (JSON format)',

    `scenario_all_cases`     longtext COMMENT 'scenario all cases (JSON format)',
    `scenario_failure_cases` longtext COMMENT 'scenario failure cases (JSON format)',

    `load_all_Cases`         longtext COMMENT 'load all cases (JSON format)',
    `load_failure_cases`     longtext COMMENT 'load failure cases (JSON format)',

    PRIMARY KEY (`id`),
    UNIQUE KEY `test_plan_report_id` (`test_plan_report_id`)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;


# 复制测试计划权限
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+COPY', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_TRACK_PLAN:READ+COPY', 'PROJECT_TRACK_PLAN');

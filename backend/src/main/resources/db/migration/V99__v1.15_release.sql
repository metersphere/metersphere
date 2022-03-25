CREATE TABLE `issue_follow`
(
    `issue_id`  varchar(50) DEFAULT NULL,
    `follow_id` varchar(50) DEFAULT NULL,
    UNIQUE KEY `issue_follow_pk` (`issue_id`, `follow_id`),
    KEY `issue_follow_follow_id_index` (`follow_id`),
    KEY `issue_follow_issue_id_index` (`issue_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- group
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_group', 'PROJECT_GROUP:READ', 'PROJECT_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_group', 'PROJECT_GROUP:READ+CREATE', 'PROJECT_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_group', 'PROJECT_GROUP:READ+EDIT', 'PROJECT_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_group', 'PROJECT_GROUP:READ+DELETE', 'PROJECT_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_group', 'PROJECT_GROUP:READ+SETTING_PERMISSION', 'PROJECT_GROUP');

ALTER TABLE test_plan_load_case
    ADD advanced_configuration TEXT NULL;

-- environment group
ALTER TABLE api_scenario
    ADD environment_type varchar(20) NULL;

ALTER TABLE api_scenario
    ADD environment_json longtext NULL;

ALTER TABLE api_scenario
    ADD environment_group_id varchar(50) NULL;

UPDATE api_scenario
SET environment_json = JSON_EXTRACT(api_scenario.scenario_definition, '$.environmentMap')
WHERE api_scenario.environment_json IS NULL;

UPDATE api_scenario
SET environment_type = 'JSON';

ALTER TABLE test_plan_api_scenario
    ADD environment_type varchar(20) NULL COMMENT '场景使用的环境类型';

ALTER TABLE test_plan_api_scenario
    ADD environment_group_id varchar(50) NULL COMMENT '场景使用的环境组ID';

UPDATE test_plan_api_scenario
SET test_plan_api_scenario.environment_type = 'JSON'
WHERE test_plan_api_scenario.environment_type IS NULL;

CREATE TABLE `environment_group`
(
    `id`           varchar(50) NOT NULL COMMENT '环境组id',
    `name`         varchar(50) NOT NULL COMMENT '环境组名',
    `workspace_id` varchar(64) NOT NULL COMMENT '所属工作空间',
    `description`  varchar(255) DEFAULT NULL COMMENT '环境组描述',
    `create_user`  varchar(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`  bigint(13)   DEFAULT NULL COMMENT '创建时间',
    `update_time`  bigint(13)   DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


CREATE TABLE `environment_group_project`
(
    `id`                   varchar(50) NOT NULL,
    `environment_group_id` varchar(50)  DEFAULT NULL COMMENT '环境组id',
    `environment_id`       varchar(50)  DEFAULT NULL COMMENT 'api test environment 环境ID',
    `project_id`           varchar(50)  DEFAULT NULL COMMENT '项目id',
    `description`          varchar(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;



INSERT INTO custom_field (id, name, scene, `type`, remark, `options`, `system`, `global`, workspace_id, create_time,
                          update_time)
VALUES ('e392af07-fdfe-4475-a459-87d59f0b1625', '测试阶段', 'PLAN', 'select', '',
        '[{"text":"test_track.plan.smoke_test","value":"smoke","system": true},{"text":"test_track.plan.system_test","value":"system","system": true},{"text":"test_track.plan.regression_test","value":"regression","system": true}]',
        1, 1, 'global', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

ALTER TABLE api_definition_exec_result
    MODIFY COLUMN name VARCHAR(100);

CREATE TABLE IF NOT EXISTS `enterprise_test_report`
(
    `id`             varchar(50)  NOT NULL COMMENT 'Test ID',
    `project_id`     varchar(50)  NOT NULL COMMENT 'Project ID this report belongs to',
    `create_time`    bigint(13)   NOT NULL COMMENT 'Create timestamp',
    `update_time`    bigint(13)   NOT NULL COMMENT 'Update timestamp',
    `create_user`    varchar(64) DEFAULT NULL COMMENT 'User ID',
    `update_user`    varchar(64) DEFAULT NULL COMMENT 'User ID',
    `name`           varchar(255) NOT NULL COMMENT 'report name',
    `status`         varchar(64) DEFAULT NULL COMMENT 'Status of email',
    `send_freq`      varchar(64) DEFAULT NULL COMMENT 'send freq',
    `send_cron`      varchar(64) DEFAULT NULL COMMENT 'send cron',
    `last_send_time` bigint(13)  DEFAULT NULL COMMENT 'last send time',
    `report_content` longtext COMMENT 'report content',
    `addressee`      longtext COMMENT 'report content',
    `duplicated`     longtext COMMENT 'report content',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `enterprise_test_report_send_record`
(
    `id`                        varchar(50) NOT NULL COMMENT 'Test ID',
    `enterprise_test_report_id` varchar(50) NOT NULL COMMENT 'Enterprise report ID this record belongs to',
    `create_time`               bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `create_user`               varchar(64) DEFAULT NULL COMMENT 'User ID',
    `status`                    varchar(64) DEFAULT NULL COMMENT 'Status of email',
    `report_content`            longtext COMMENT 'report content',
    `addressee`                 longtext COMMENT 'report content',
    `duplicated`                longtext COMMENT 'report content',
    PRIMARY KEY (`id`),
    INDEX `enterprise_test_report_id` (`enterprise_test_report_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;


CREATE INDEX issues_platform_id_IDX ON issues (platform_id);

ALTER TABLE issues
    MODIFY COLUMN title varchar(300);
ALTER TABLE issues
    MODIFY COLUMN description LONGTEXT;

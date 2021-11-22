CREATE TABLE `issue_follow`
(
    `issue_id`  varchar(50) DEFAULT NULL,
    `follow_id` varchar(50) DEFAULT NULL,
    UNIQUE KEY `issue_follow_pk` (`issue_id`, `follow_id`),
    KEY `issue_follow_follow_id_index` (`follow_id`),
    KEY `issue_follow_issue_id_index` (`issue_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

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

alter table test_plan_load_case
    add advanced_configuration TEXT null;


INSERT INTO custom_field (id, name, scene, `type`, remark, `options`, `system`, `global`, workspace_id, create_time,
                          update_time)
VALUES ('e392af07-fdfe-4475-a459-87d59f0b1625', '测试阶段', 'PLAN', 'select', '',
        '[{"text":"test_track.plan.smoke_test","value":"smoke","system": true},{"text":"test_track.plan.system_test","value":"system","system": true},{"text":"test_track.plan.regression_test","value":"regression","system": true}]',
        1, 1, 'global', unix_timestamp() * 1000, unix_timestamp() * 1000);

ALTER TABLE api_definition_exec_result MODIFY COLUMN name VARCHAR (100);

CREATE TABLE IF NOT EXISTS `enterprise_test_report` (
    `id` varchar(50)  NOT NULL COMMENT 'Test ID',
    `project_id` varchar(50)  NOT NULL COMMENT 'Project ID this report belongs to',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    `create_user` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `update_user` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `name` varchar(255)  NOT NULL COMMENT 'report name',
    `status` varchar(64)  DEFAULT NULL COMMENT 'Status of email',
    `send_freq` varchar(64)  DEFAULT NULL COMMENT 'send freq',
    `send_cron` varchar(64)  DEFAULT NULL COMMENT 'send cron',
    `last_send_time` bigint(13)  DEFAULT NULL COMMENT 'last send time',
    `report_content` longtext COMMENT 'report content',
    `addressee` longtext COMMENT 'report content',
    `duplicated` longtext COMMENT 'report content',
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `enterprise_test_report_send_record` (
    `id` varchar(50)  NOT NULL COMMENT 'Test ID',
    `enterprise_test_report_id` varchar(50)  NOT NULL COMMENT 'Enterprise report ID this record belongs to',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `create_user` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `status` varchar(64)  DEFAULT NULL COMMENT 'Status of email',
    `report_content` longtext COMMENT 'report content',
    `addressee` longtext COMMENT 'report content',
    `duplicated` longtext COMMENT 'report content',
    PRIMARY KEY (`id`),
    INDEX `enterprise_test_report_id` (`enterprise_test_report_id`) USING BTREE
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;


CREATE INDEX issues_platform_id_IDX ON issues (platform_id);

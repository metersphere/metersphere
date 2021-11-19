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

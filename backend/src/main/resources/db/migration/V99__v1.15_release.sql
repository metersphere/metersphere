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



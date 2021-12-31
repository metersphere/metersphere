CREATE TABLE IF NOT EXISTS `error_report_library` (
    `id` varchar(50)  NOT NULL COMMENT 'Test ID',
    `project_id` varchar(50)  NOT NULL COMMENT 'Project ID this report belongs to',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    `create_user` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `update_user` varchar(64) DEFAULT NULL COMMENT 'User ID',
    `error_code` varchar(255)  NOT NULL COMMENT 'error code',
    `match_type` varchar(255)  NOT NULL COMMENT 'match type',
    `status` tinyint(1) NULL DEFAULT NULL COMMENT 'status',
    content  longtext COMMENT 'content',
    `description` longtext COMMENT 'report content',
    PRIMARY KEY (`id`),
    INDEX `project_id` (`project_id`) USING BTREE,
    INDEX `project_id_status` (`project_id`,`status`) USING BTREE
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `api_definition_exec_result` ADD `error_code` varchar(255);
ALTER TABLE `api_scenario_report_result` ADD `error_code` varchar(255);

-- 赋予admin默认的权限
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'PROJECT_ERROR_REPORT_LIBRARY:READ', 'PROJECT_ERROR_REPORT_LIBRARY');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'PROJECT_ERROR_REPORT_LIBRARY:READ+CREATE', 'PROJECT_ERROR_REPORT_LIBRARY');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'PROJECT_ERROR_REPORT_LIBRARY:READ+EDIT', 'PROJECT_ERROR_REPORT_LIBRARY');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'PROJECT_ERROR_REPORT_LIBRARY:READ+DELETE', 'PROJECT_ERROR_REPORT_LIBRARY');
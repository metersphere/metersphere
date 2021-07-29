-- 项目管理员
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_REPORT_ANALYSIS:READ', 'PROJECT_REPORT_ANALYSIS');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_REPORT_ANALYSIS:READ+EXPORT', 'PROJECT_REPORT_ANALYSIS');

-- 项目成员
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_REPORT_ANALYSIS:READ', 'PROJECT_REPORT_ANALYSIS');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_REPORT_ANALYSIS:READ+EXPORT', 'PROJECT_REPORT_ANALYSIS');

-- 只读
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'read_only', 'PROJECT_REPORT_ANALYSIS:READ', 'PROJECT_REPORT_ANALYSIS');


-- 重建中间表
DROP TABLE load_test_report_detail;
CREATE TABLE `load_test_report_detail`
(
    `report_id` VARCHAR(50)           NOT NULL,
    `part`      BIGINT AUTO_INCREMENT NOT NULL,
    `content`   LONGTEXT,
    PRIMARY KEY (`report_id`, `part`)
)
    ENGINE = MyISAM
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;
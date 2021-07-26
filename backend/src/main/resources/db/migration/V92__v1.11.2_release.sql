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


-- 关联场景测试和性能测试，一键更新性能测试
CREATE TABLE IF NOT EXISTS `api_load_test` (
    `id`                varchar(50) NOT NULL COMMENT 'ID',
    `api_id`            varchar(255) NOT NULL COMMENT 'Relate resource id',
    `load_test_id`      varchar(50) NOT NULL COMMENT 'Load Test id',
    `env_id`            varchar(50) NULL COMMENT 'Api case env id',
    `type`              varchar(20) NOT NULL COMMENT 'Api Type',
    `api_version`       int(10) DEFAULT 0 NULL COMMENT 'Relate Scenario Version',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- 添加版本号
ALTER TABLE api_test_case
    ADD version INT(10) DEFAULT 0 NULL COMMENT '版本号';

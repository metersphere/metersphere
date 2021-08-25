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
CREATE TABLE IF NOT EXISTS `load_test_report_detail` (
     `part`      BIGINT(11) AUTO_INCREMENT NOT NULL,
     `report_id` VARCHAR(50)               NOT NULL,
     `content`   LONGTEXT,
     PRIMARY KEY (`part`, `report_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

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

-- 测试计划资源表
CREATE TABLE IF NOT EXISTS test_plan_report_resource  (
    `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `test_plan_report_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    `resource_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    `resource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    `execute_result` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `test_plan_report_id`(`test_plan_report_id`) USING BTREE,
    INDEX `resource_id`(`resource_id`) USING BTREE,
    INDEX `resource_type`(`resource_type`) USING BTREE,
    INDEX `report_resource_id`(`test_plan_report_id`,`resource_id`) USING BTREE,
    INDEX `report_resource_type_id`(`test_plan_report_id`,`resource_id`,`resource_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

alter table project add azure_devops_id varchar(50) null after zentao_id;
alter table custom_field_template modify default_value varchar(100) null ;

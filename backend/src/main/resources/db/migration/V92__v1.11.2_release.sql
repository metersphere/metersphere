-- v1.10-lts 升级的基线是 v92，v91不会执行，这里把v91的内容加到这里，保证lts可以正常升级
-- test resource pool
ALTER TABLE test_resource_pool
    ADD api TINYINT(1) NULL;

ALTER TABLE test_resource_pool
    ADD performance TINYINT(1) NULL;

UPDATE test_resource_pool
SET api         = TRUE,
    performance = TRUE
WHERE type = 'NODE';

UPDATE test_resource_pool
SET api         = FALSE,
    performance = TRUE
WHERE type = 'K8S';
ALTER table api_scenario_report add actuator varchar(100) null;
ALTER table api_definition_exec_result add actuator varchar(100) null;
ALTER table api_definition_exec_result add trigger_mode varchar(50) null;

-- 功能案例、接口、接口案例、场景增加字段以及旧数据管理
ALTER TABLE api_test_case ADD COLUMN `status` VARCHAR(50);
ALTER TABLE api_test_case ADD COLUMN `original_status` VARCHAR(50);

UPDATE api_test_case SET `status` = 'Underway' WHERE `status` IS NULL;
UPDATE api_test_case SET `original_status` = `status` WHERE `original_status` IS NULL;

ALTER TABLE test_case ADD COLUMN `original_status` VARCHAR(50);
UPDATE test_case SET `status` = 'Underway' WHERE `status` IS NULL;
UPDATE test_case SET `original_status` = `status` WHERE `original_status` IS NULL;

ALTER TABLE api_test_case ADD COLUMN `delete_time` bigint(13) COMMENT 'Delete timestamp';
ALTER TABLE api_test_case ADD COLUMN `delete_user_id` varchar(64) COMMENT 'Delete user id';

ALTER TABLE test_case ADD COLUMN `delete_time` bigint(13) COMMENT 'Delete timestamp';
ALTER TABLE test_case ADD COLUMN `delete_user_id` varchar(64) COMMENT 'Delete user id';

ALTER TABLE api_definition ADD COLUMN `delete_time` bigint(13) COMMENT 'Delete timestamp';
ALTER TABLE api_definition ADD COLUMN `delete_user_id` varchar(64) COMMENT 'Delete user id';

ALTER TABLE api_scenario ADD COLUMN `delete_time` bigint(13) COMMENT 'Delete timestamp';
ALTER TABLE api_scenario ADD COLUMN `delete_user_id` varchar(64) COMMENT 'Delete user id';

-- 更新之前删除过的接口数据和场景数据,删除人/删除时间字段
UPDATE api_definition SET delete_time = update_time,delete_user_id = user_id WHERE `status` = 'Trash' AND  delete_time is null;
UPDATE api_scenario SET delete_time = update_time,delete_user_id = create_user WHERE `status` = 'Trash' AND  delete_time is null;

-- 更新接口状态为“Trash'但是案例状态不是Trash的数据
UPDATE api_test_case testCase INNER JOIN (SELECT t1.id FROM api_test_case  t1 INNER JOIN api_definition t2 ON t1.api_definition_id = t2.id
                                          WHERE (t1.`status` is null  or t1.`status` != 'Trash') and t2.`status` = 'Trash') table1 ON testCase.id = table1.id SET `STATUS` = 'Trash';
-- 功能案例、接口、接口案例、场景增加字段以及旧数据管理结束

-- 场景增加外键表：引用的ID
CREATE TABLE api_scenario_reference_id  (
                                            `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
                                            `api_scenario_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                            `create_time` bigint(13) NULL DEFAULT NULL,
                                            `create_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                            `reference_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                            `reference_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                            `data_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                            PRIMARY KEY (`id`) USING BTREE,
                                            INDEX `reference_id`(`reference_id`) USING BTREE,
                                            INDEX `api_scenario_id`(`api_scenario_id`) USING BTREE,
                                            INDEX `data_type`(`data_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;



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

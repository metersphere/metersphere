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

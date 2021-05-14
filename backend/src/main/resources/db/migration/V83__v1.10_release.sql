-- load_test_report
ALTER TABLE load_test_report
    ADD project_id VARCHAR(50) NULL;

ALTER TABLE load_test_report
    ADD test_name VARCHAR(64) NULL;

ALTER TABLE load_test_report
    ADD jmx_content LONGTEXT NULL;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.project_id = load_test.project_id;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.test_name = load_test.name;
-- api_environment_running_param
CREATE TABLE IF NOT EXISTS api_environment_running_param  (
                                                              `id`                varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL,
                                                              `api_enviroment_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL,
                                                              `key`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                                              `value`             longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NULL,
                                                              `create_time`       bigint(13)                                                    NULL DEFAULT NULL,
                                                              `update_time`       bigint(13)                                                    NULL DEFAULT NULL,
                                                              `create_user_id`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL,
                                                              `update_user_id`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL,
                                                              PRIMARY KEY (`id`) USING BTREE,
                                                              INDEX `api_enviroment_id` (`api_enviroment_id`) USING BTREE,
                                                              INDEX `key_api_enviroment_id` (`api_enviroment_id`, `key`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = Dynamic;

-- schedule
alter table schedule
    add config VARCHAR(500) null;


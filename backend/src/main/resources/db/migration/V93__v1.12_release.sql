-- load_test_report_result_part
CREATE TABLE `load_test_report_result_part`
(
    `report_id`      VARCHAR(50) NOT NULL,
    `report_key`     VARCHAR(64) NOT NULL,
    `resource_index` INT         NOT NULL,
    `report_value`   LONGTEXT,
    PRIMARY KEY `load_test_report_result_report_id_report_key_index` (`report_id`, `report_key`, `resource_index`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;

ALTER TABLE test_resource_pool
    ADD backend_listener TINYINT(1) DEFAULT 1;
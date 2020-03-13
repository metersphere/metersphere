CREATE TABLE IF NOT EXISTS `fuc_test` (
    `id`                     varchar(50) NOT NULL COMMENT 'Test ID',
    `project_id`             varchar(50) NOT NULL COMMENT 'Project ID this test belongs to',
    `name`                   varchar(64) NOT NULL COMMENT 'Test name',
    `description`            varchar(255) DEFAULT NULL COMMENT 'Test description',
    `runtime_configuration`     longtext COMMENT 'Load configuration (JSON format)',
    `schedule`               longtext COMMENT 'Test schedule (cron list)',
    `create_time`            bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`            bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `fuc_test_file` (
    `test_id` varchar(64) DEFAULT NULL,
    `file_id` varchar(64) DEFAULT NULL,
    UNIQUE KEY `load_test_file_unique_key` (`test_id`, `file_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4 COMMENT ='功能测试和文件的关联表';

CREATE TABLE IF NOT EXISTS `fuc_test_report` (
    `id`          varchar(50) NOT NULL COMMENT 'Test report ID',
    `test_id`     varchar(50) NOT NULL COMMENT 'Test ID this test report belongs to',
    `name`        varchar(64) NOT NULL COMMENT 'Test report name',
    `description` varchar(255) DEFAULT NULL COMMENT 'Test report name',
    `content`     longtext,
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    `status`      varchar(64) NOT NULL COMMENT 'Status of this test run',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;


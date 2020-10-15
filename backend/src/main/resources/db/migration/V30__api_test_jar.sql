CREATE TABLE IF NOT EXISTS `api_test_jar_config` (
    `id`          varchar(50) NOT NULL COMMENT 'ID',
    `name`        varchar(64) NOT NULL COMMENT 'Name',
    `file_name`        varchar(64) NOT NULL COMMENT 'File name',
    `owner`       varchar(50) NOT NULL COMMENT 'User ID',
    `path`        varchar(255) NOT NULL COMMENT 'File path',
    `project_id`  varchar(50) NOT NULL COMMENT 'Project ID this jar belongs to',
    `description` varchar(255) DEFAULT NULL COMMENT 'description',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
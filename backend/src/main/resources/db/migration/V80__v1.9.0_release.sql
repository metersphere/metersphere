-- issues template and test case template
CREATE TABLE IF NOT EXISTS `custom_field` (
    `id`          varchar(50) NOT NULL COMMENT 'Custom field ID',
    `name`        varchar(64) NOT NULL COMMENT 'Custom field name',
    `scene`        varchar(30) NOT NULL COMMENT 'Custom field use scene',
    `type`        varchar(30) NOT NULL COMMENT 'Custom field type',
    `remark` varchar(255) DEFAULT NULL COMMENT 'Custom field remark',
    `options`     text DEFAULT NULL COMMENT 'Test resource pool status',
    `workspace_id` varchar(50) DEFAULT NULL COMMENT 'Workspace ID this custom field belongs to',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `field_template` (
    `id`          varchar(50) NOT NULL COMMENT 'Field template ID',
    `name`        varchar(64) NOT NULL COMMENT 'Field template name',
    `platform`    varchar(30) NOT NULL COMMENT 'Field template platform',
    `description` varchar(255) DEFAULT NULL COMMENT 'Field template description',
    `scene`       varchar(30) NOT NULL COMMENT 'Field template use scene',
    `workspace_id` varchar(50) DEFAULT NULL COMMENT 'Workspace ID this field template belongs to',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

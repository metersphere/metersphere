-- issues template and test case template
CREATE TABLE IF NOT EXISTS `custom_field` (
    `id`            varchar(50) NOT NULL COMMENT 'Custom field ID',
    `name`          varchar(64) NOT NULL COMMENT 'Custom field name',
    `scene`         varchar(30) NOT NULL COMMENT 'Custom field use scene',
    `type`          varchar(30) NOT NULL COMMENT 'Custom field type',
    `remark`        varchar(255) DEFAULT NULL COMMENT 'Custom field remark',
    `options`       text DEFAULT NULL COMMENT 'Test resource pool status',
    `workspace_id`  varchar(50) DEFAULT NULL COMMENT 'Workspace ID this custom field belongs to',
    `create_time`   bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`   bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `test_case_field_template` (
    `id`                varchar(50) NOT NULL COMMENT 'Field template ID',
    `name`              varchar(64) NOT NULL COMMENT 'Field template name',
    `type`              varchar(30) NOT NULL COMMENT 'Field template type',
    `description`       varchar(255) DEFAULT NULL COMMENT 'Field template description',
    `case_name`         varchar(64) NOT NULL COMMENT 'Test Case Name',
    `workspace_id`      varchar(50) DEFAULT NULL COMMENT 'Workspace ID this field template belongs to',
    `prerequisite`      varchar(255) DEFAULT NULL COMMENT 'Test case prerequisite condition',
    `step_description`  text DEFAULT NULL COMMENT 'Test case steps desc',
    `expected_result`   text DEFAULT NULL COMMENT 'Test case expected result',
    `actual_result`     text DEFAULT NULL COMMENT 'Test case actual result',
    `create_time`       bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`       bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `issue_template` (
    `id`                varchar(50) NOT NULL COMMENT 'Field template ID',
    `name`              varchar(64) NOT NULL COMMENT 'Field template name',
    `platform`          varchar(30) NOT NULL COMMENT 'Field template type',
    `description`       varchar(255) DEFAULT NULL COMMENT 'Field template description',
    `title`             varchar(64) NOT NULL COMMENT 'Issue title',
    `workspace_id`      varchar(50) DEFAULT NULL COMMENT 'Workspace ID this field template belongs to',
    `content`           text DEFAULT NULL COMMENT 'Issue content',
    `create_time`       bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`       bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `custom_field_template` (
    `id`                varchar(50) NOT NULL COMMENT 'Custom field field template related id',
    `field_id`          varchar(50) NOT NULL COMMENT 'Custom field ID',
    `template_id`       varchar(50) NOT NULL COMMENT 'Field template ID',
    `scene`             varchar(30) NOT NULL COMMENT 'Use scene',
    `required`          tinyint(1) DEFAULT NULL COMMENT 'Is required',
    `order`             int(11)  DEFAULT NULL COMMENT 'Item order',
    `default_value`     varchar(30) DEFAULT NULL COMMENT 'Default value',
    `custom_data`       varchar(255) DEFAULT NULL COMMENT 'Custom data',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `api_module` (
    `id`                     varchar(50) NOT NULL COMMENT 'Test case node ID',
    `project_id`             varchar(50) NOT NULL COMMENT 'Project ID this node belongs to',
    `name`                   varchar(64) NOT NULL COMMENT 'Node name',
    `protocol`               varchar(64) NOT NULL COMMENT 'Node protocol',
    `parent_id`              varchar(50) DEFAULT NULL COMMENT 'Parent node ID',
    `level`                  int(10)  DEFAULT 1 COMMENT 'Node level',
    `create_time`            bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`            bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;
CREATE TABLE IF NOT EXISTS `test_plan` (
    `id`                     varchar(50) NOT NULL COMMENT 'Test Plan ID',
    `project_id`             varchar(50) NOT NULL COMMENT 'Project ID this plan belongs to',
    `name`                   varchar(64) NOT NULL COMMENT 'Plan name',
    `description`            varchar(255) DEFAULT NULL COMMENT 'Plan description',
    `status`                 varchar(20) NOT NULL COMMENT 'Plan status',
    `test_case_match_rule`   varchar(255) DEFAULT NULL COMMENT 'Test case match rule',
    `executor_match_rule`    varchar(255) DEFAULT NULL  COMMENT 'Executor match rule)',
    `tags`                   text COMMENT 'Test plan tags (JSON format)',
    `create_time`            bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`            bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`project_id`) references project(`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;


CREATE TABLE IF NOT EXISTS `test_case_node` (
    `id`                     int(13) PRIMARY KEY AUTO_INCREMENT COMMENT 'Test case node ID',
    `project_id`             varchar(50) NOT NULL COMMENT 'Project ID this node belongs to',
    `name`                   varchar(64) NOT NULL COMMENT 'Node name',
    `p_id`                   varchar(50) NOT NULL COMMENT 'Parent node ID',
    `order`                  bigint(13)  COMMENT 'Node order',
    `create_time`            bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`            bigint(13)  NOT NULL COMMENT 'Update timestamp',
    FOREIGN KEY (`project_id`) references project(`id`)
)
    AUTO_INCREMENT = 1
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;


CREATE TABLE IF NOT EXISTS `test_case` (
    `id`                     varchar(50) NOT NULL COMMENT 'Test case ID',
    `node_id`                int(13) NOT NULL COMMENT 'Node ID this case belongs to',
    `project_id`             varchar(50) NOT NULL COMMENT 'Project ID this test belongs to',
    `name`                   varchar(64) NOT NULL COMMENT 'Case name',
    `type`                   varchar(25) NOT NULL COMMENT 'Test case type',
    `priority`               varchar(10) DEFAULT NULL COMMENT 'Test case priority',
    `method`                 varchar(15) NOT NULL COMMENT 'Test case method type',
    `prerequisite`           varchar(255) DEFAULT NULL COMMENT 'Test case prerequisite condition',
    `detail`                 text COMMENT 'Load configuration (JSON format)',
    `steps`                  text COMMENT 'Test case steps (JSON format)',
    `tags`                   text COMMENT 'Test case tags (JSON format)',
    `create_time`            bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`            bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`project_id`) references project(`id`),
    FOREIGN KEY (`node_id`) references test_case_node(`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;


CREATE TABLE IF NOT EXISTS `test_plan_test_case` (
    `id`                     int(13) PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `plan_id`                varchar(50) NOT NULL COMMENT 'Plan ID relation to',
    `case_id`                varchar(50) NOT NULL COMMENT 'Case ID relation to',
    `executor`               varchar(64) NOT NULL COMMENT 'Test case executor',
    `status`                 varchar(15) NULL COMMENT 'Test case status',
    `results`                longtext COMMENT 'Test case result',
    `remark`                 varchar(255) DEFAULT NULL COMMENT 'Test case remark',
    `create_time`            bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`            bigint(13)  NOT NULL COMMENT 'Update timestamp',
    FOREIGN KEY (`plan_id`) references test_plan(`id`),
    FOREIGN KEY (`case_id`) references test_case(`id`)
)
    AUTO_INCREMENT = 1
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;
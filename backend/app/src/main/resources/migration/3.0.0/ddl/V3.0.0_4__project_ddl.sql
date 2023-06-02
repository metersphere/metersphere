-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS `project`
(
    `id`           VARCHAR(50) NOT NULL COMMENT '项目ID',
    `workspace_id` VARCHAR(50) NOT NULL COMMENT '工作空间ID',
    `name`         VARCHAR(64) NOT NULL COMMENT '项目名称',
    `description`  VARCHAR(255) DEFAULT NULL COMMENT '项目描述',
    `create_time`  BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`  BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`  VARCHAR(100) DEFAULT NULL COMMENT '创建人',
    `system_id`    VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `project_workspace_id_index` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='项目';

CREATE TABLE IF NOT EXISTS `project_application`
(
    `project_id` VARCHAR(50) NOT NULL COMMENT '项目ID',
    `type`       VARCHAR(50) NOT NULL COMMENT '配置项',
    `type_value` VARCHAR(255) DEFAULT NULL COMMENT '配置值',
    PRIMARY KEY (`project_id`, `type`),
    UNIQUE KEY `project_application_pk` (`project_id`, `type`),
    KEY `project_application_project_id_index` (`project_id`),
    KEY `project_application_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='项目应用';

CREATE TABLE IF NOT EXISTS `project_extend`
(
    `project_id`          VARCHAR(50) NOT NULL COMMENT '项目ID',
    `tapd_id`             VARCHAR(50)          DEFAULT NULL,
    `jira_key`            VARCHAR(50)          DEFAULT NULL,
    `zentao_id`           VARCHAR(50)          DEFAULT NULL,
    `azure_devops_id`     VARCHAR(50)          DEFAULT NULL,
    `case_template_id`    VARCHAR(50)          DEFAULT NULL COMMENT '用例模版ID',
    `azure_filter_id`     VARCHAR(50)          DEFAULT NULL COMMENT 'azure 过滤需求的 parent workItem ID',
    `platform`            VARCHAR(20) NOT NULL DEFAULT 'Local' COMMENT '项目使用哪个平台的模板',
    `third_part_template` TINYINT(1)           DEFAULT '0' COMMENT '是否使用第三方平台缺陷模板',
    `version_enable`      TINYINT(1)           DEFAULT '1' COMMENT '是否开启版本管理',
    `issue_config`        LONGBLOB,
    `api_template_id`     VARCHAR(64)          DEFAULT NULL,
    PRIMARY KEY (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='项目扩展';

CREATE TABLE IF NOT EXISTS `project_version`
(
    `id`           VARCHAR(50)  NOT NULL COMMENT '版本ID',
    `project_id`   VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `name`         VARCHAR(100) NOT NULL COMMENT '版本名称',
    `description`  VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `status`       VARCHAR(20)  DEFAULT NULL COMMENT '状态',
    `latest`       TINYINT(1)   NOT NULL COMMENT '是否是最新版',
    `publish_time` BIGINT       DEFAULT NULL COMMENT '发布时间',
    `start_time`   BIGINT       DEFAULT NULL COMMENT '开始时间',
    `end_time`     BIGINT       DEFAULT NULL COMMENT '结束时间',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间',
    `create_user`  VARCHAR(100) NOT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_name` (`name`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_latest` (`latest`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='版本管理';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
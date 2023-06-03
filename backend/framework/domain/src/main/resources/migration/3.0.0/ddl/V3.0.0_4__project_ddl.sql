-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

--
-- Table structure for table `custom_field`
--


CREATE TABLE IF NOT EXISTS `custom_field`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '自定义字段ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '自定义字段名称',
    `scene`       VARCHAR(30)  NOT NULL COMMENT '使用场景',
    `type`        VARCHAR(30)  NOT NULL COMMENT '自定义字段类型',
    `remark`      VARCHAR(255)          DEFAULT NULL COMMENT '自定义字段备注',
    `options`     TEXT COMMENT '自定义字段选项',
    `system`      BIT(1)                DEFAULT b'0' COMMENT '是否是系统字段',
    `global`      BIT(1)                DEFAULT b'0' COMMENT '是否是全局字段',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)           DEFAULT NULL COMMENT '创建人',
    `project_id`  VARCHAR(50)           DEFAULT NULL COMMENT '项目ID',
    `third_part`  BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否关联第三方',
    PRIMARY KEY (`id`),
    KEY `idx_global` (`global`),
    KEY `idx_name` (`name`),
    KEY `idx_scene` (`scene`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='自定义字段';


--
-- Table structure for table `custom_field_template`
--


CREATE TABLE IF NOT EXISTS `custom_field_template`
(
    `id`            VARCHAR(50) NOT NULL COMMENT '自定义模版ID',
    `field_id`      VARCHAR(50) NOT NULL COMMENT '自定义字段ID',
    `template_id`   VARCHAR(50) NOT NULL COMMENT '模版ID',
    `scene`         VARCHAR(30) NOT NULL COMMENT '使用场景',
    `required`      BIT(1)       DEFAULT NULL COMMENT '是否必填',
    `pos`           INT          DEFAULT NULL COMMENT '排序字段',
    `default_value` LONGBLOB COMMENT '默认值',
    `custom_data`   VARCHAR(255) DEFAULT NULL COMMENT '自定义数据',
    `key`           VARCHAR(1)   DEFAULT NULL COMMENT '自定义表头',
    PRIMARY KEY (`id`),
    KEY `custom_field_template_field_id_index` (`field_id`),
    KEY `custom_field_template_template_id_index` (`template_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='自定义模版';


--
-- Table structure for table `custom_function`
--


CREATE TABLE IF NOT EXISTS `custom_function`
(
    `id`          VARCHAR(50)  NOT NULL,
    `name`        VARCHAR(255) NOT NULL COMMENT '函数名',
    `tags`        VARCHAR(1000) DEFAULT NULL COMMENT '标签',
    `description` VARCHAR(500)  DEFAULT NULL COMMENT '函数描述',
    PRIMARY KEY (`id`),
    KEY `name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='自定义函数-代码片段';


--
-- Table structure for table `fake_error`
--


CREATE TABLE IF NOT EXISTS `fake_error`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '误报ID',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(64)  NOT NULL COMMENT '创建人',
    `update_user` VARCHAR(64)  NOT NULL COMMENT '更新人',
    `error_code`  VARCHAR(255) NOT NULL COMMENT '错误码',
    `match_type`  VARCHAR(255) NOT NULL COMMENT '匹配类型',
    `status`      BIT(1) DEFAULT NULL COMMENT '状态',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `project_id_status` (`project_id`, `status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_update_user` (`update_user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='误报库';


--
-- Table structure for table `file_metadata`
--


CREATE TABLE IF NOT EXISTS `file_metadata`
(
    `id`            VARCHAR(50)  NOT NULL COMMENT '文件ID',
    `name`          VARCHAR(255) NOT NULL COMMENT '文件名',
    `type`          VARCHAR(64)           DEFAULT NULL COMMENT '文件类型',
    `size`          BIGINT       NOT NULL COMMENT '文件大小',
    `create_time`   BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`   BIGINT       NOT NULL COMMENT '更新时间',
    `project_id`    VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `storage`       VARCHAR(50)  NOT NULL DEFAULT 'MINIO' COMMENT '文件存储方式',
    `create_user`   VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user`   VARCHAR(50)  NOT NULL COMMENT '修改人',
    `tags`          VARCHAR(1000)         DEFAULT NULL COMMENT '标签',
    `description`   VARCHAR(500)          DEFAULT NULL COMMENT '描述',
    `module_id`     VARCHAR(50)           DEFAULT NULL COMMENT '文件所属模块',
    `load_jar`      BIT(1)                DEFAULT b'0' COMMENT '是否加载jar（开启后用于接口测试执行时使用）',
    `path`          VARCHAR(1000)         DEFAULT NULL COMMENT '文件存储路径',
    `resource_type` VARCHAR(50)           DEFAULT NULL COMMENT '资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据',
    `latest`        BIT(1)       NOT NULL DEFAULT b'1' COMMENT '是否是最新版',
    `ref_id`        VARCHAR(50)  NOT NULL COMMENT '同版本数据关联的ID',
    PRIMARY KEY (`id`),
    KEY `idx_file_name` (`name`),
    KEY `idx_latest` (`latest`),
    KEY `idx_ref_id` (`ref_id`),
    KEY `idx_storage` (`storage`),
    KEY `idx_module_id` (`module_id`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文件基础信息';


--
-- Table structure for table `file_module`
--


CREATE TABLE IF NOT EXISTS `file_module`
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `project_id`  VARCHAR(50) NOT NULL COMMENT '项目ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '模块名称',
    `parent_id`   VARCHAR(50) DEFAULT NULL COMMENT '父级ID',
    `level`       INT         DEFAULT '1' COMMENT '层数',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `pos`         DOUBLE      DEFAULT NULL COMMENT '排序用的标识',
    `create_user` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `module_type` VARCHAR(20) DEFAULT 'module' COMMENT '模块类型: module/repository',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_name` (`name`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_timed` (`update_time`),
    KEY `idx_pos` (`pos`),
    KEY `idx_create_user` (`create_user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文件管理模块';


--
-- Table structure for table `project`
--


CREATE TABLE IF NOT EXISTS `project`
(
    `id`           VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `workspace_id` VARCHAR(50)  NOT NULL COMMENT '工作空间ID',
    `name`         VARCHAR(255) NOT NULL COMMENT '项目名称',
    `description`  VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`  BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`  VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `system_id`    VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_workspace_id` (`workspace_id`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_create_time` (`create_time`),
    KEY `idex_update_time` (`update_time`),
    KEY `idx_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='项目';


--
-- Table structure for table `project_application`
--


CREATE TABLE IF NOT EXISTS `project_application`
(
    `project_id` VARCHAR(50) NOT NULL COMMENT '项目ID',
    `type`       VARCHAR(50) NOT NULL COMMENT '配置项',
    `type_value` VARCHAR(255) DEFAULT NULL COMMENT '配置值',
    PRIMARY KEY (`project_id`, `type`),
    KEY `idx_project_application_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='项目应用';


--
-- Table structure for table `project_version`
--


CREATE TABLE IF NOT EXISTS `project_version`
(
    `id`           VARCHAR(50)  NOT NULL COMMENT '版本ID',
    `project_id`   VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `name`         VARCHAR(255) NOT NULL COMMENT '版本名称',
    `description`  VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `status`       VARCHAR(20)  DEFAULT NULL COMMENT '状态',
    `latest`       BIT(1)       NOT NULL COMMENT '是否是最新版',
    `publish_time` BIGINT       DEFAULT NULL COMMENT '发布时间',
    `start_time`   BIGINT       DEFAULT NULL COMMENT '开始时间',
    `end_time`     BIGINT       DEFAULT NULL COMMENT '结束时间',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间',
    `create_user`  VARCHAR(50)  NOT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_name` (`name`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_latest` (`latest`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='版本管理';


--
-- Table structure for table `file_module_blob`
--


CREATE TABLE IF NOT EXISTS `file_module_blob`
(
    `id`                   VARCHAR(50) NOT NULL COMMENT 'ID',
    `repository_desc`      LONGBLOB COMMENT '存储库描述',
    `repository_path`      VARCHAR(255) DEFAULT NULL COMMENT '存储库路径',
    `repository_user_name` VARCHAR(255) DEFAULT NULL COMMENT '存储库Token',
    `repository_token`     VARCHAR(255) DEFAULT NULL COMMENT '存储库Token',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文件管理模块大字段';


--
-- Table structure for table `custom_function_blob`
--


CREATE TABLE IF NOT EXISTS `custom_function_blob`
(
    `id`     VARCHAR(50) NOT NULL,
    `params` LONGBLOB COMMENT '参数列表',
    `script` LONGBLOB COMMENT '函数体',
    `result` LONGBLOB COMMENT '执行结果',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='自定义函数-代码片段大字段';


--
-- Table structure for table `fake_error_blob`
--


CREATE TABLE IF NOT EXISTS `fake_error_blob`
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'Test ID',
    `content`     LONGBLOB COMMENT '内容',
    `description` LONGBLOB COMMENT '报告内容',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='误报库大字段';


--
-- Table structure for table `file_metadata_blob`
--


CREATE TABLE IF NOT EXISTS `file_metadata_blob`
(
    `id`       VARCHAR(50) NOT NULL COMMENT '文件ID',
    `git_info` LONGBLOB COMMENT '储存库',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文件基础信息大字段';


--
-- Table structure for table `project_extend`
--


CREATE TABLE IF NOT EXISTS `project_extend`
(
    `id`                  VARCHAR(50) NOT NULL COMMENT '项目ID',
    `tapd_id`             VARCHAR(50)          DEFAULT NULL,
    `jira_key`            VARCHAR(50)          DEFAULT NULL,
    `zentao_id`           VARCHAR(50)          DEFAULT NULL,
    `azure_devops_id`     VARCHAR(50)          DEFAULT NULL,
    `case_template_id`    VARCHAR(50)          DEFAULT NULL COMMENT '用例模版ID',
    `azure_filter_id`     VARCHAR(50)          DEFAULT NULL COMMENT 'azure 过滤需求的 parent workItem ID',
    `platform`            VARCHAR(20) NOT NULL DEFAULT 'Local' COMMENT '项目使用哪个平台的模板',
    `third_part_template` BIT(1)               DEFAULT b'0' COMMENT '是否使用第三方平台缺陷模板',
    `version_enable`      BIT(1)               DEFAULT b'1' COMMENT '是否开启版本管理',
    `issue_config`        LONGBLOB,
    `api_template_id`     VARCHAR(64)          DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='项目扩展';


--
-- Table structure for table `functional_case_template`
--


CREATE TABLE IF NOT EXISTS `functional_case_template`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `description` VARCHAR(500)          DEFAULT NULL COMMENT '描述',
    `system`      BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否是系统模板',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_system` (`system`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='功能用例模版';


--
-- Table structure for table `functional_case_template_extend`
--


CREATE TABLE IF NOT EXISTS `functional_case_template_extend`
(
    `id`               VARCHAR(50) NOT NULL COMMENT '模板ID',
    `case_name`        VARCHAR(255)         DEFAULT NULL COMMENT '用例名称模板',
    `prerequisite`     TEXT COMMENT '前置条件模板',
    `step_description` TEXT COMMENT '步骤描述模板',
    `expected_result`  TEXT COMMENT '预期结果模板',
    `actual_result`    TEXT COMMENT '实际结果模板',
    `step_model`       VARCHAR(64) NOT NULL DEFAULT 'Step' COMMENT '编辑模式模板：步骤模式/文本模式',
    `steps`            TEXT COMMENT '用例步骤',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='功能用例模版扩展';


--
-- Table structure for table `bug_template_extend`
--


CREATE TABLE IF NOT EXISTS `bug_template_extend`
(
    `id`      VARCHAR(50) NOT NULL COMMENT '缺陷模板ID',
    `title`   VARCHAR(255) DEFAULT NULL COMMENT '缺陷标题模板',
    `content` TEXT COMMENT '缺陷内容模板',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='缺陷模板扩展';


--
-- Table structure for table `bug_template`
--


CREATE TABLE IF NOT EXISTS `bug_template`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `description` VARCHAR(500)          DEFAULT NULL COMMENT '描述',
    `system`      BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否是系统模板',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_system` (`system`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='缺陷模版';


--
-- Table structure for table `api_template`
--


CREATE TABLE IF NOT EXISTS `api_template`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `description` VARCHAR(500)          DEFAULT NULL COMMENT '描述',
    `system`      BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否是系统模板',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_system` (`system`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='接口定义模板';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
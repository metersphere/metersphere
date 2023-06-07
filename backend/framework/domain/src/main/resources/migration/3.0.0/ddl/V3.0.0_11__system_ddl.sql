-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

--
-- Table structure for table `auth_source`
--

CREATE TABLE IF NOT EXISTS `auth_source`
(
    `id`            VARCHAR(50) NOT NULL COMMENT '认证源ID',
    `configuration` BLOB        NOT NULL COMMENT '认证源配置',
    `status`        VARCHAR(64) NOT NULL COMMENT '状态 启用 禁用',
    `create_time`   BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`   BIGINT      NOT NULL COMMENT '更新时间',
    `description`   VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `name`          VARCHAR(255) DEFAULT NULL COMMENT '名称',
    `type`          VARCHAR(30)  DEFAULT NULL COMMENT '类型',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='三方认证源';

--
-- Table structure for table `user_role`
--

CREATE TABLE IF NOT EXISTS `user_role`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '组ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '组名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `system`      BIT(1)       NOT NULL COMMENT '是否是系统用户组',
    `type`        VARCHAR(20)  NOT NULL COMMENT '所属类型',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人(操作人）',
    `scope_id`    VARCHAR(50)  NOT NULL COMMENT '应用范围',
    PRIMARY KEY (`id`),
    KEY `idx_group_name` (`name`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_scope_id` (`scope_id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户组';

--
-- Table structure for table `license`
--

CREATE TABLE IF NOT EXISTS `license`
(
    `id`           VARCHAR(50) NOT NULL COMMENT 'ID',
    `create_time`  BIGINT      NOT NULL COMMENT 'Create timestamp',
    `update_time`  BIGINT      NOT NULL COMMENT 'Update timestamp',
    `license_code` LONGTEXT COMMENT 'license_code',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

--
-- Table structure for table `message_task`
--

CREATE TABLE IF NOT EXISTS `message_task`
(
    `id`          VARCHAR(50)  NOT NULL,
    `type`        VARCHAR(50)  NOT NULL COMMENT '消息类型',
    `event`       VARCHAR(255) NOT NULL COMMENT '通知事件类型',
    `receiver`    VARCHAR(50)  NOT NULL COMMENT '接收人id',
    `task_type`   VARCHAR(64)  NOT NULL COMMENT '任务类型',
    `webhook`     VARCHAR(255)          DEFAULT NULL COMMENT 'webhook地址',
    `test_id`     VARCHAR(50)  NOT NULL DEFAULT 'none' COMMENT '具体测试的ID',
    `create_time` BIGINT       NOT NULL DEFAULT '0' COMMENT '创建时间',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_receiver` (`receiver`),
    KEY `idx_test_id` (`test_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='消息通知任务';

--
-- Table structure for table `notification`
--

CREATE TABLE IF NOT EXISTS `notification`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `type`          VARCHAR(30)  NOT NULL COMMENT '通知类型',
    `receiver`      VARCHAR(50)  NOT NULL COMMENT '接收人',
    `title`         VARCHAR(255) NOT NULL COMMENT '标题',
    `status`        VARCHAR(30)  NOT NULL COMMENT '状态',
    `create_time`   BIGINT       NOT NULL COMMENT '创建时间',
    `operator`      VARCHAR(50)  NOT NULL COMMENT '操作人',
    `operation`     VARCHAR(50)  NOT NULL COMMENT '操作',
    `resource_id`   VARCHAR(50)  NOT NULL COMMENT '资源ID',
    `resource_type` VARCHAR(50)  NOT NULL COMMENT '资源类型',
    `resource_name` VARCHAR(255) NOT NULL COMMENT '资源名称',
    PRIMARY KEY (`id`),
    KEY `idx_receiver` (`receiver`),
    KEY `idx_receiver_type` (`receiver`, `type`),
    KEY `idx_notification_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='消息通知';

--
-- Table structure for table `novice_statistics`
--

CREATE TABLE IF NOT EXISTS `novice_statistics`
(
    `id`          VARCHAR(50) NOT NULL,
    `user_id`     VARCHAR(50)          DEFAULT NULL COMMENT '用户id',
    `guide_step`  BIT(1)      NOT NULL DEFAULT b'0' COMMENT '新手引导完成的步骤',
    `guide_num`   INT         NOT NULL DEFAULT '1' COMMENT '新手引导的次数',
    `data_option` LONGBLOB COMMENT 'data option (JSON format)',
    `create_time` BIGINT               DEFAULT NULL,
    `update_time` BIGINT               DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='新手村';

--
-- Table structure for table `operating_log`
--

CREATE TABLE IF NOT EXISTS `operating_log`
(
    `id`           VARCHAR(50) NOT NULL COMMENT 'ID',
    `project_id`   VARCHAR(50) NOT NULL COMMENT '项目ID',
    `oper_method`  VARCHAR(500)  DEFAULT NULL COMMENT 'operating method',
    `create_user`  VARCHAR(100)  DEFAULT NULL COMMENT '创建人',
    `oper_user`    VARCHAR(50)   DEFAULT NULL COMMENT '操作人',
    `source_id`    VARCHAR(6000) DEFAULT NULL COMMENT '资源ID',
    `oper_type`    VARCHAR(100)  DEFAULT NULL COMMENT '操作类型',
    `oper_module`  VARCHAR(100)  DEFAULT NULL COMMENT '操作模块',
    `oper_title`   VARCHAR(6000) DEFAULT NULL COMMENT '操作标题',
    `oper_path`    VARCHAR(500)  DEFAULT NULL COMMENT '操作路径',
    `oper_content` LONGBLOB COMMENT '操作内容',
    `oper_params`  LONGBLOB COMMENT '操作参数',
    `oper_time`    BIGINT      NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_module` (`oper_module`),
    KEY `idx_oper_project_id` (`project_id`),
    KEY `idx_oper_time_index` (`oper_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='操作日志';

--
-- Table structure for table `operating_log_resource`
--

CREATE TABLE IF NOT EXISTS `operating_log_resource`
(
    `id`               VARCHAR(50) NOT NULL COMMENT 'ID',
    `operating_log_id` VARCHAR(50) NOT NULL COMMENT 'Operating log ID',
    `source_id`        VARCHAR(50) NOT NULL COMMENT 'operating source id',
    PRIMARY KEY (`id`),
    KEY `operating_log_id_index` (`operating_log_id`),
    KEY `source_id_index` (`source_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='操作日志关系记录';

--
-- Table structure for table `plugin`
--

CREATE TABLE IF NOT EXISTS `plugin`
(
    `id`           VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`         VARCHAR(255) DEFAULT NULL COMMENT 'plugin name',
    `plugin_id`    VARCHAR(300) NOT NULL COMMENT 'Plugin id',
    `script_id`    VARCHAR(300) NOT NULL COMMENT 'Ui script id',
    `clazz_name`   VARCHAR(500) NOT NULL COMMENT 'Plugin clazzName',
    `jmeter_clazz` VARCHAR(300) NOT NULL COMMENT 'Jmeter base clazzName',
    `source_path`  VARCHAR(300) NOT NULL COMMENT 'Plugin jar path',
    `source_name`  VARCHAR(300) NOT NULL COMMENT 'Plugin jar name',
    `exec_entry`   VARCHAR(300) DEFAULT NULL COMMENT 'plugin init entry class',
    `create_time`  BIGINT       DEFAULT NULL,
    `update_time`  BIGINT       DEFAULT NULL,
    `create_user`  VARCHAR(50)  DEFAULT NULL,
    `xpack`        BIT(1)       DEFAULT NULL COMMENT 'Is xpack plugin',
    `scenario`     VARCHAR(50)  NOT NULL COMMENT 'Plugin usage scenarios',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='插件';

--
-- Table structure for table `quota`
--

CREATE TABLE IF NOT EXISTS `quota`
(
    `id`              VARCHAR(50) NOT NULL,
    `api`             INT            DEFAULT NULL COMMENT '接口数量',
    `performance`     INT            DEFAULT NULL COMMENT '性能测试数量',
    `max_threads`     INT            DEFAULT NULL COMMENT '最大并发数',
    `duration`        INT            DEFAULT NULL COMMENT '最大执行时长',
    `resource_pool`   VARCHAR(1000)  DEFAULT NULL COMMENT '资源池列表',
    `organization_id` VARCHAR(50)    DEFAULT NULL COMMENT '组织ID',
    `use_default`     BIT(1)         DEFAULT NULL COMMENT '是否使用默认值',
    `update_time`     BIGINT         DEFAULT NULL COMMENT '更新时间',
    `member`          INT            DEFAULT NULL COMMENT '成员数量限制',
    `project`         INT            DEFAULT NULL COMMENT '项目数量限制',
    `project_id`      VARCHAR(50)    DEFAULT NULL COMMENT '项目类型配额',
    `vum_total`       DECIMAL(10, 2) DEFAULT NULL COMMENT '总vum数',
    `vum_used`        DECIMAL(10, 2) DEFAULT NULL COMMENT '消耗的vum数',
    PRIMARY KEY (`id`),
    KEY `idx_quota_project_id` (`project_id`),
    KEY `idx_quota_organization_id` (`organization_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='配额';

--
-- Table structure for table `schedule`
--

CREATE TABLE IF NOT EXISTS `schedule`
(
    `id`          VARCHAR(50)  NOT NULL,
    `key`         VARCHAR(50)   DEFAULT NULL COMMENT 'qrtz UUID',
    `type`        VARCHAR(50)  NOT NULL COMMENT '资源类型',
    `value`       VARCHAR(255) NOT NULL COMMENT 'Schedule value',
    `job`         VARCHAR(64)  NOT NULL COMMENT 'Schedule Job Class Name',
    `enable`      BIT(1)        DEFAULT NULL COMMENT 'Schedule Eable',
    `resource_id` VARCHAR(50)   DEFAULT NULL,
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `create_time` BIGINT        DEFAULT NULL COMMENT 'Create timestamp',
    `update_time` BIGINT        DEFAULT NULL COMMENT 'Update timestamp',
    `project_id`  VARCHAR(50)   DEFAULT NULL COMMENT '项目ID',
    `name`        VARCHAR(255)  DEFAULT NULL COMMENT '名称',
    `config`      VARCHAR(1000) DEFAULT NULL COMMENT '配置',
    PRIMARY KEY (`id`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_enable` (`enable`),
    KEY `idx_name` (`name`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='定时任务';

--
-- Table structure for table `service_integration`
--

CREATE TABLE IF NOT EXISTS `service_integration`
(
    `id`              VARCHAR(50) NOT NULL,
    `platform`        VARCHAR(50) NOT NULL COMMENT '平台',
    `configuration`   BLOB        NOT NULL,
    `organization_id` VARCHAR(50) DEFAULT NULL COMMENT '组织ID',
    PRIMARY KEY (`id`),
    KEY `idx_organization_id` (`organization_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='服务集成';

--
-- Table structure for table `system_parameter`
--

CREATE TABLE IF NOT EXISTS `system_parameter`
(
    `param_key`   VARCHAR(64)  NOT NULL COMMENT '参数名称',
    `param_value` VARCHAR(255)          DEFAULT NULL COMMENT '参数值',
    `type`        VARCHAR(100) NOT NULL DEFAULT 'text' COMMENT '类型',
    PRIMARY KEY (`param_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='系统参数';

--
-- Table structure for table `test_resource`
--

CREATE TABLE IF NOT EXISTS `test_resource`
(
    `id`                    VARCHAR(50) NOT NULL COMMENT '资源节点ID',
    `test_resource_pool_id` VARCHAR(50) NOT NULL COMMENT '资源池ID',
    `configuration`         BLOB COMMENT '资源节点配置',
    `status`                VARCHAR(20) NOT NULL COMMENT '资源节点状态',
    `create_time`           BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`           BIGINT      NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_resource_pool_id` (`test_resource_pool_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='测试资源池节点';

--
-- Table structure for table `test_resource_pool`
--

CREATE TABLE IF NOT EXISTS `test_resource_pool`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '资源池ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `type`        VARCHAR(30)  NOT NULL COMMENT '类型',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `status`      VARCHAR(20)  NOT NULL COMMENT '状态',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `image`       VARCHAR(100) DEFAULT NULL COMMENT '性能测试镜像',
    `heap`        VARCHAR(200) DEFAULT NULL COMMENT '性能测试jvm配置',
    `gc_algo`     VARCHAR(200) DEFAULT NULL COMMENT '性能测试gc配置',
    `create_user` VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `api`         BIT(1)       DEFAULT NULL COMMENT '是否用于接口测试',
    `performance` BIT(1)       DEFAULT NULL COMMENT '是否用于性能测试',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_create_user` (`create_user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='测试资源池';

--
-- Table structure for table `user`
--

CREATE TABLE IF NOT EXISTS `user`
(
    `id`                   VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `name`                 VARCHAR(255) NOT NULL COMMENT '用户名',
    `email`                VARCHAR(64)  NOT NULL COMMENT '用户邮箱',
    `password`             VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户密码',
    `status`               VARCHAR(50)  NOT NULL COMMENT '用户状态，启用或禁用',
    `create_time`          BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`          BIGINT       NOT NULL COMMENT '更新时间',
    `language`             VARCHAR(30)                                            DEFAULT NULL COMMENT '语言',
    `last_organization_id` VARCHAR(50)                                            DEFAULT NULL COMMENT '当前组织ID',
    `phone`                VARCHAR(50)                                            DEFAULT NULL COMMENT '手机号',
    `source`               VARCHAR(50)  NOT NULL COMMENT '来源：LOCAL OIDC CAS',
    `last_project_id`      VARCHAR(50)                                            DEFAULT NULL COMMENT '当前项目ID',
    `create_user`          VARCHAR(50)  NOT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_email` (`email`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_organization_id` (`last_organization_id`),
    KEY `idx_project_id` (`last_project_id`),
    KEY `idx_create_user` (`create_user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户';

--
-- Table structure for table `user_role_relation`
--

CREATE TABLE IF NOT EXISTS `user_role_relation`
(
    `id`          VARCHAR(50) NOT NULL COMMENT '用户组关系ID',
    `user_id`     VARCHAR(50) NOT NULL COMMENT '用户ID',
    `role_id`     VARCHAR(50) NOT NULL COMMENT '组ID',
    `source_id`   VARCHAR(50) NOT NULL COMMENT '组织或项目ID',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_group_id` (`role_id`),
    KEY `idx_source_id` (`source_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户组关系';

--
-- Table structure for table `user_role_permission`
--

CREATE TABLE IF NOT EXISTS `user_role_permission`
(
    `id`            VARCHAR(64)  NOT NULL,
    `role_id`       VARCHAR(64)  NOT NULL COMMENT '用户组ID',
    `permission_id` VARCHAR(128) NOT NULL COMMENT '权限ID',
    `module_id`     VARCHAR(64)  NOT NULL COMMENT '功能菜单',
    PRIMARY KEY (`id`),
    KEY `idx_group_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户组权限';

--
-- Table structure for table `user_key`
--

CREATE TABLE IF NOT EXISTS `user_key`
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'user_key ID',
    `create_user` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `access_key`  VARCHAR(50) NOT NULL COMMENT 'access_key',
    `secret_key`  VARCHAR(50) NOT NULL COMMENT 'secret key',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `status`      VARCHAR(10) DEFAULT NULL COMMENT '状态',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_ak` (`access_key`),
    KEY `idx_create_user` (`create_user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户api key';

--
-- Table structure for table `organization`
--

CREATE TABLE IF NOT EXISTS `organization`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '组织ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '组织名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='组织';

--
-- Table structure for table `user_extend`
--

CREATE TABLE IF NOT EXISTS `user_extend`
(
    `id`              VARCHAR(50) NOT NULL COMMENT '用户ID',
    `platform_info`   BLOB COMMENT '其他平台对接信息',
    `selenium_server` VARCHAR(255) DEFAULT NULL COMMENT 'UI本地调试地址',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户扩展';

--
-- Table structure for table `message_task_blob`
--

CREATE TABLE IF NOT EXISTS `message_task_blob`
(
    `id`       VARCHAR(50) NOT NULL,
    `template` TEXT COMMENT '消息模版',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='消息通知任务大字段';

--
-- Table structure for table `plugin_blob`
--

CREATE TABLE IF NOT EXISTS `plugin_blob`
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `form_option` LONGBLOB COMMENT 'plugin form option',
    `form_script` LONGBLOB COMMENT 'plugin form script',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='插件大字段';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
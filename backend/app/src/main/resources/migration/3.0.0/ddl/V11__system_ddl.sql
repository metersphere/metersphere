-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS `user`
(
    `id`                VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `name`              VARCHAR(64)  NOT NULL COMMENT '用户名',
    `email`             VARCHAR(64)  NOT NULL COMMENT '用户邮箱',
    `password`          VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户密码',
    `status`            VARCHAR(50)  NOT NULL COMMENT '用户状态，启用或禁用',
    `create_time`       BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`       BIGINT       NOT NULL COMMENT '更新时间',
    `language`          VARCHAR(30)                                            DEFAULT NULL COMMENT '语言',
    `last_workspace_id` VARCHAR(50)                                            DEFAULT NULL COMMENT '当前工作空间ID',
    `phone`             VARCHAR(50)                                            DEFAULT NULL COMMENT '手机号',
    `source`            VARCHAR(50)  NOT NULL COMMENT '来源：LOCAL OIDC CAS',
    `last_project_id`   VARCHAR(50)                                            DEFAULT NULL COMMENT '当前项目ID',
    `create_user`       VARCHAR(100) NOT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_email` (`email`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_workspace_id` (`last_workspace_id`),
    KEY `idx_project_id` (`last_project_id`),
    KEY `idx_create_user` (`create_user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户';

CREATE TABLE IF NOT EXISTS `user_extend`
(
    `id`         VARCHAR(50) NOT NULL COMMENT '用户ID',
    `platform_info`   BLOB COMMENT '其他平台对接信息',
    `selenium_server` VARCHAR(255) DEFAULT NULL COMMENT 'UI本地调试地址',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户扩展';

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

CREATE TABLE IF NOT EXISTS `user_role`
(
    `id`          VARCHAR(50) NOT NULL COMMENT '组ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '组名称',
    `description` VARCHAR(100) DEFAULT NULL COMMENT '描述',
    `system`      BIT(1)      NOT NULL COMMENT '是否是系统用户组',
    `type`        VARCHAR(20) NOT NULL COMMENT '所属类型',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(64) NOT NULL COMMENT '创建人(操作人）',
    `scope_id`    VARCHAR(64) NOT NULL COMMENT '应用范围',
    PRIMARY KEY (`id`),
    KEY `idx_group_name` (`name`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_scope_id` (`scope_id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户组';

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

CREATE TABLE IF NOT EXISTS `user_role_relation`
(
    `id`          VARCHAR(50) NOT NULL COMMENT '用户组关系ID',
    `user_id`     VARCHAR(50) NOT NULL COMMENT '用户ID',
    `role_id`     VARCHAR(50) NOT NULL COMMENT '组ID',
    `source_id`   VARCHAR(50) NOT NULL COMMENT '工作空间或项目ID',
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

CREATE TABLE IF NOT EXISTS `workspace`
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '工作空间ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '工作空间名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
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
  COLLATE = utf8mb4_general_ci COMMENT ='工作空间';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
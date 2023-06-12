-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

DROP TABLE IF EXISTS auth_source;
CREATE TABLE auth_source
(
    `id`            VARCHAR(50) NOT NULL COMMENT '认证源ID',
    `configuration` BLOB        NOT NULL COMMENT '认证源配置',
    `enable`        BIT         NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time`   BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`   BIGINT      NOT NULL COMMENT '更新时间',
    `description`   VARCHAR(500) COMMENT '描述',
    `name`          VARCHAR(255) COMMENT '名称',
    `type`          VARCHAR(30) COMMENT '类型',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '三方认证源';


CREATE INDEX idx_name ON auth_source (`name`);
CREATE INDEX idx_create_time ON auth_source (`create_time`);
CREATE INDEX idx_update_time ON auth_source (`update_time`);

DROP TABLE IF EXISTS user_role;
CREATE TABLE user_role
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '组ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '组名称',
    `description` VARCHAR(500) COMMENT '描述',
    `internal`    BIT          NOT NULL COMMENT '是否是内置用户组',
    `type`        VARCHAR(20)  NOT NULL COMMENT '所属类型 SYSTEM ORGANIZATION PROJECT',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人(操作人）',
    `scope_id`    VARCHAR(50)  NOT NULL COMMENT '应用范围',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户组';


CREATE INDEX idx_group_name ON user_role (`name`);
CREATE INDEX idx_create_time ON user_role (`create_time`);
CREATE INDEX idx_create_user ON user_role (`create_user`);
CREATE INDEX idx_scope_id ON user_role (`scope_id`);
CREATE INDEX idx_update_time ON user_role (`update_time`);

DROP TABLE IF EXISTS license;
CREATE TABLE license
(
    `id`           VARCHAR(50) NOT NULL COMMENT 'ID',
    `create_time`  BIGINT      NOT NULL COMMENT 'Create timestamp',
    `update_time`  BIGINT      NOT NULL COMMENT 'Update timestamp',
    `license_code` LONGTEXT COMMENT 'license_code',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '';

DROP TABLE IF EXISTS message_task;
CREATE TABLE message_task
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '',
    `type`        VARCHAR(50)  NOT NULL COMMENT '消息类型',
    `event`       VARCHAR(255) NOT NULL COMMENT '通知事件类型',
    `receiver`    VARCHAR(50)  NOT NULL COMMENT '接收人id',
    `task_type`   VARCHAR(64)  NOT NULL COMMENT '任务类型',
    `webhook`     VARCHAR(255) COMMENT 'webhook地址',
    `test_id`     VARCHAR(50)  NOT NULL DEFAULT 'none' COMMENT '具体测试的ID',
    `create_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '消息通知任务';


CREATE INDEX idx_project_id ON message_task (`project_id`);
CREATE INDEX idx_create_time ON message_task (`create_time`);
CREATE INDEX idx_receiver ON message_task (`receiver`);
CREATE INDEX idx_test_id ON message_task (`test_id`);

DROP TABLE IF EXISTS notification;
CREATE TABLE notification
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
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '消息通知';


CREATE INDEX idx_receiver ON notification (`receiver`);
CREATE INDEX idx_receiver_type ON notification (`receiver`, `type`);
CREATE INDEX idx_notification_create_time ON notification (`create_time`);

DROP TABLE IF EXISTS novice_statistics;
CREATE TABLE novice_statistics
(
    `id`          VARCHAR(50) NOT NULL COMMENT '',
    `user_id`     VARCHAR(50) COMMENT '用户id',
    `guide_step`  BIT         NOT NULL DEFAULT 0 COMMENT '新手引导完成的步骤',
    `guide_num`   INT         NOT NULL DEFAULT 1 COMMENT '新手引导的次数',
    `data_option` LONGBLOB COMMENT 'data option (JSON format)',
    `create_time` BIGINT COMMENT '',
    `update_time` BIGINT COMMENT '',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '新手村';

DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log(
                              `id` VARCHAR(50) NOT NULL   COMMENT '主键' ,
                              `project_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '项目id' ,
                              `create_time` BIGINT NOT NULL   COMMENT '操作时间' ,
                              `create_user` VARCHAR(50)    COMMENT '操作人' ,
                              `source_id` VARCHAR(50)    COMMENT '资源id' ,
                              `method` VARCHAR(255) NOT NULL   COMMENT '操作方法' ,
                              `type` VARCHAR(20) NOT NULL   COMMENT '操作类型/add/update/delete' ,
                              `module` VARCHAR(20)    COMMENT '操作模块/api/case/scenario/ui' ,
                              `details` VARCHAR(1000)    COMMENT '操作详情' ,
                              `path` VARCHAR(255)    COMMENT '操作路径' ,
                              PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '操作日志';

CREATE INDEX idx_create_time ON operation_log(`create_time`);
CREATE INDEX idx_create_user ON operation_log(`create_user`);
CREATE INDEX idx_method ON operation_log(`method`);
CREATE INDEX idx_module ON operation_log(`module`);
CREATE INDEX idx_project_id ON operation_log(`project_id`);
CREATE INDEX idx_type ON operation_log(`type`);

DROP TABLE IF EXISTS plugin;
CREATE TABLE plugin
(
    `id`           VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`         VARCHAR(255) COMMENT 'plugin name',
    `plugin_id`    VARCHAR(300) NOT NULL COMMENT 'Plugin id',
    `script_id`    VARCHAR(300) NOT NULL COMMENT 'Ui script id',
    `clazz_name`   VARCHAR(500) NOT NULL COMMENT 'Plugin clazzName',
    `jmeter_clazz` VARCHAR(300) NOT NULL COMMENT 'Jmeter base clazzName',
    `source_path`  VARCHAR(300) NOT NULL COMMENT 'Plugin jar path',
    `source_name`  VARCHAR(300) NOT NULL COMMENT 'Plugin jar name',
    `exec_entry`   VARCHAR(300) COMMENT 'plugin init entry class',
    `create_time`  BIGINT COMMENT '',
    `update_time`  BIGINT COMMENT '',
    `create_user`  VARCHAR(50) COMMENT '',
    `xpack`        BIT COMMENT 'Is xpack plugin',
    `scenario`     VARCHAR(50)  NOT NULL COMMENT 'Plugin usage scenarios',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '插件';

DROP TABLE IF EXISTS quota;
CREATE TABLE quota
(
    `id`                    VARCHAR(50) NOT NULL COMMENT '',
    `organization_id`       VARCHAR(50) COMMENT '组织ID',
    `project_id`            VARCHAR(50) COMMENT '项目类型配额',
    `functional_case`       INT COMMENT '功能用例数量',
    `load_test_vum_total`   DECIMAL(10, 2) COMMENT '总vum数',
    `load_test_vum_used`    DECIMAL(10, 2) COMMENT '消耗的vum数',
    `load_test_max_threads` INT COMMENT '最大并发数',
    `load_test_duration`    INT COMMENT '最大执行时长',
    `resource_pool`         VARCHAR(1000) COMMENT '资源池列表',
    `use_default`           BIT COMMENT '是否使用默认值',
    `update_time`           BIGINT COMMENT '更新时间',
    `member`                INT COMMENT '成员数量限制',
    `project`               INT COMMENT '项目数量限制',
    `api_test_vum_total`    DECIMAL(10, 2) COMMENT '总vum数',
    `api_test_vum_used`     DECIMAL(10, 2) COMMENT '消耗的vum数',
    `ui_test_vum_total`     DECIMAL(10, 2) COMMENT '总vum数',
    `ui_test_vum_used`      DECIMAL(10, 2) COMMENT '消耗的vum数',
    `file_storage`          BIGINT COMMENT '文件大小限制',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '配额';


CREATE INDEX idx_quota_project_id ON quota (`project_id`);
CREATE INDEX idx_quota_workspace_id ON quota (`organization_id`);

DROP TABLE IF EXISTS schedule;
CREATE TABLE schedule
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '',
    `key`         VARCHAR(50) COMMENT 'qrtz UUID',
    `type`        VARCHAR(50)  NOT NULL COMMENT '资源类型',
    `value`       VARCHAR(255) NOT NULL COMMENT 'cron 表达式',
    `job`         VARCHAR(64)  NOT NULL COMMENT 'Schedule Job Class Name',
    `enable`      BIT COMMENT '是否开启',
    `resource_id` VARCHAR(50) COMMENT '资源ID，api_scenario ui_scenario load_test',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `project_id`  VARCHAR(50) COMMENT '项目ID',
    `name`        VARCHAR(100) COMMENT '名称',
    `config`      VARCHAR(1000) COMMENT '配置',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时任务';


CREATE INDEX idx_resource_id ON schedule (`resource_id`);
CREATE INDEX idx_create_user ON schedule (`create_user`);
CREATE INDEX idx_create_time ON schedule (`create_time`);
CREATE INDEX idx_update_time ON schedule (`update_time`);
CREATE INDEX idx_project_id ON schedule (`project_id`);
CREATE INDEX idx_enable ON schedule (`enable`);
CREATE INDEX idx_name ON schedule (`name`);
CREATE INDEX idx_type ON schedule (`type`);

DROP TABLE IF EXISTS service_integration;
CREATE TABLE service_integration
(
    `id`              VARCHAR(50) NOT NULL COMMENT '',
    `platform`        VARCHAR(50) NOT NULL COMMENT '平台',
    `configuration`   BLOB        NOT NULL COMMENT '',
    `organization_id` VARCHAR(50) COMMENT '组织ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '服务集成';


CREATE INDEX idx_workspace_id ON service_integration (`organization_id`);

DROP TABLE IF EXISTS system_parameter;
CREATE TABLE system_parameter
(
    `param_key`   VARCHAR(64)  NOT NULL COMMENT '参数名称',
    `param_value` VARCHAR(255) COMMENT '参数值',
    `type`        VARCHAR(100) NOT NULL DEFAULT 'text' COMMENT '类型',
    PRIMARY KEY (param_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '系统参数';

DROP TABLE IF EXISTS test_resource;
CREATE TABLE test_resource
(
    `id`                    VARCHAR(50) NOT NULL COMMENT '资源节点ID',
    `test_resource_pool_id` VARCHAR(50) NOT NULL COMMENT '资源池ID',
    `configuration`         BLOB COMMENT '资源节点配置',
    `status`                VARCHAR(20) NOT NULL COMMENT '资源节点状态',
    `create_time`           BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`           BIGINT      NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '测试资源池节点';


CREATE INDEX idx_resource_pool_id ON test_resource (`test_resource_pool_id`);
CREATE INDEX idx_status ON test_resource (`status`);
CREATE INDEX idx_create_time ON test_resource (`create_time`);
CREATE INDEX idx_update_time ON test_resource (`update_time`);

DROP TABLE IF EXISTS test_resource_pool;
CREATE TABLE test_resource_pool
(
    `id`              VARCHAR(50)  NOT NULL COMMENT '资源池ID',
    `name`            VARCHAR(255) NOT NULL COMMENT '名称',
    `type`            VARCHAR(30)  NOT NULL COMMENT '类型',
    `description`     VARCHAR(500) COMMENT '描述',
    `enable`          BIT          NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `load_test_image` VARCHAR(100) COMMENT '性能测试镜像',
    `load_test_heap`  VARCHAR(200) COMMENT '性能测试jvm配置',
    `create_user`     VARCHAR(50) COMMENT '创建人',
    `api_test`        BIT COMMENT '是否用于接口测试',
    `load_test`       BIT COMMENT '是否用于性能测试',
    `ui_test`         BIT COMMENT '是否用于ui测试',
    `grid`            VARCHAR(255) COMMENT 'ui测试grid配置',
    `server_url`      VARCHAR(255) COMMENT 'ms部署地址',
    `deleted`         BIT          NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '测试资源池';


CREATE INDEX idx_name ON test_resource_pool (`name`);
CREATE INDEX idx_type ON test_resource_pool (`type`);
CREATE INDEX idx_status ON test_resource_pool (`enable`);
CREATE INDEX idx_create_time ON test_resource_pool (`create_time`);
CREATE INDEX idx_update_time ON test_resource_pool (`update_time`);
CREATE INDEX idx_create_user ON test_resource_pool (`create_user`);

DROP TABLE IF EXISTS user;
CREATE TABLE user
(
    `id`                   VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `name`                 VARCHAR(255) NOT NULL COMMENT '用户名',
    `email`                VARCHAR(64)  NOT NULL COMMENT '用户邮箱',
    `password`             VARCHAR(256) COLLATE utf8mb4_bin COMMENT '用户密码',
    `enable`               BIT          NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time`          BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`          BIGINT       NOT NULL COMMENT '更新时间',
    `language`             VARCHAR(30) COMMENT '语言',
    `last_organization_id` VARCHAR(50) COMMENT '当前组织ID',
    `phone`                VARCHAR(50) COMMENT '手机号',
    `source`               VARCHAR(50)  NOT NULL COMMENT '来源：LOCAL OIDC CAS OAUTH2',
    `last_project_id`      VARCHAR(50) COMMENT '当前项目ID',
    `create_user`          VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user`          VARCHAR(50)  NOT NULL COMMENT '修改人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户';


CREATE INDEX idx_name ON user (`name`);
CREATE UNIQUE INDEX idx_email ON user (`email`);
CREATE INDEX idx_create_time ON user (`create_time`);
CREATE INDEX idx_update_time ON user (`update_time`);
CREATE INDEX idx_organization_id ON user (`last_organization_id`);
CREATE INDEX idx_project_id ON user (`last_project_id`);
CREATE INDEX idx_create_user ON user (`create_user`);
CREATE INDEX idx_update_user ON user (`update_user`);

DROP TABLE IF EXISTS user_role_relation;
CREATE TABLE user_role_relation(
`id` VARCHAR(50) NOT NULL   COMMENT '用户组关系ID' ,
`user_id` VARCHAR(50) NOT NULL   COMMENT '用户ID' ,
`role_id` VARCHAR(50) NOT NULL   COMMENT '组ID' ,
`source_id` VARCHAR(50) NOT NULL   COMMENT '组织或项目ID' ,
`create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
`create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户组关系';



CREATE INDEX idx_user_id ON user_role_relation(`user_id`);
CREATE INDEX idx_group_id ON user_role_relation(`role_id`);
CREATE INDEX idx_source_id ON user_role_relation(`source_id`);
CREATE INDEX idx_create_time ON user_role_relation(`create_time`);

DROP TABLE IF EXISTS user_role_permission;
CREATE TABLE user_role_permission
(
    `id`            VARCHAR(64)  NOT NULL COMMENT '',
    `role_id`       VARCHAR(64)  NOT NULL COMMENT '用户组ID',
    `permission_id` VARCHAR(128) NOT NULL COMMENT '权限ID',
    `module_id`     VARCHAR(64)  NOT NULL COMMENT '功能菜单',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户组权限';


CREATE INDEX idx_group_id ON user_role_permission (`role_id`);
CREATE INDEX idx_permission_id ON user_role_permission (`permission_id`);

DROP TABLE IF EXISTS user_key;
CREATE TABLE user_key
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'user_key ID',
    `create_user` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `access_key`  VARCHAR(50) NOT NULL COMMENT 'access_key',
    `secret_key`  VARCHAR(50) NOT NULL COMMENT 'secret key',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `status`      VARCHAR(10) COMMENT '状态',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户api key';


CREATE UNIQUE INDEX idx_ak ON user_key (`access_key`);
CREATE INDEX idx_create_user ON user_key (`create_user`);

DROP TABLE IF EXISTS organization;
CREATE TABLE organization
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '组织ID',
    `num`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '组织编号',
    `name`        VARCHAR(100) NOT NULL COMMENT '组织名称',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `deleted`     BIT          NOT NULL DEFAULT 0 COMMENT '是否删除',
    `delete_user` VARCHAR(50) COMMENT '删除人',
    `delete_time` BIGINT COMMENT '删除时间',
    `enable`      BIT          NOT NULL DEFAULT 1 COMMENT '是否启用',
    PRIMARY KEY (id),
    CONSTRAINT idx_num UNIQUE (num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '组织';


CREATE INDEX idx_name ON organization (`name`);
CREATE INDEX idx_create_user ON organization (`create_user`);
CREATE INDEX idx_create_time ON organization (`create_time`);
CREATE INDEX idx_update_time ON organization (`update_time`);
CREATE INDEX idx_deleted ON organization (`deleted`);

DROP TABLE IF EXISTS user_extend;
CREATE TABLE user_extend
(
    `id`              VARCHAR(50) NOT NULL COMMENT '用户ID',
    `platform_info`   BLOB COMMENT '其他平台对接信息',
    `selenium_server` VARCHAR(255) COMMENT 'UI本地调试地址',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户扩展';

DROP TABLE IF EXISTS message_task_blob;
CREATE TABLE message_task_blob
(
    `id`       VARCHAR(50) NOT NULL COMMENT '',
    `template` TEXT COMMENT '消息模版',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '消息通知任务大字段';

DROP TABLE IF EXISTS plugin_blob;
CREATE TABLE plugin_blob
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `form_option` LONGBLOB COMMENT 'plugin form option',
    `form_script` LONGBLOB COMMENT 'plugin form script',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '插件大字段';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
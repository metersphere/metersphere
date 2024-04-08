-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS auth_source
(
    `id`            VARCHAR(50) NOT NULL COMMENT '认证源ID',
    `configuration` BLOB        NOT NULL COMMENT '认证源配置',
    `enable`        BIT         NOT NULL DEFAULT 0 COMMENT '是否启用',
    `create_time`   BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`   BIGINT      NOT NULL COMMENT '更新时间',
    `description`   VARCHAR(1000) COMMENT '描述',
    `name`          VARCHAR(255) COMMENT '名称',
    `type`          VARCHAR(30) COMMENT '类型',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '三方认证源';


CREATE INDEX idx_name ON auth_source (`name`);
CREATE INDEX idx_create_time ON auth_source (`create_time` desc);
CREATE INDEX idx_update_time ON auth_source (`update_time` desc);

CREATE TABLE IF NOT EXISTS user_role
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '组ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '组名称',
    `description` VARCHAR(1000) COMMENT '描述',
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
CREATE INDEX idx_create_time ON user_role (`create_time` desc);
CREATE INDEX idx_create_user ON user_role (`create_user`);
CREATE INDEX idx_scope_id ON user_role (`scope_id`);
CREATE INDEX idx_update_time ON user_role (`update_time` desc);

CREATE TABLE IF NOT EXISTS license
(
    `id`           VARCHAR(50) NOT NULL COMMENT 'ID',
    `create_time`  BIGINT      NOT NULL COMMENT 'Create timestamp',
    `update_time`  BIGINT      NOT NULL COMMENT 'Update timestamp',
    `license_code` LONGTEXT COMMENT 'license_code',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '';

CREATE TABLE IF NOT EXISTS novice_statistics
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


CREATE TABLE IF NOT EXISTS plugin
(
    `id` VARCHAR(100) NOT NULL   COMMENT 'ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '插件名称' ,
    `plugin_id` VARCHAR(300) NOT NULL   COMMENT '插件ID（名称加版本号）' ,
    `file_name` VARCHAR(300) NOT NULL   COMMENT '文件名' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `enable` BIT NOT NULL  DEFAULT 1 COMMENT '是否启用插件' ,
    `global` BIT NOT NULL  DEFAULT 1 COMMENT '是否是全局插件' ,
    `xpack` BIT NOT NULL  DEFAULT 0 COMMENT '是否是企业版插件' ,
    `description` VARCHAR(1000)    COMMENT '插件描述' ,
    `scenario` VARCHAR(50) NOT NULL   COMMENT '插件使用场景API_PROTOCOL/PLATFORM' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '插件';

CREATE TABLE IF NOT EXISTS plugin_script
(
    `plugin_id` VARCHAR(50) NOT NULL   COMMENT '插件的ID' ,
    `script_id` VARCHAR(50) NOT NULL   COMMENT '插件中对应表单配置的ID' ,
    `name` VARCHAR(255)    COMMENT '插件中对应表单配置的名称' ,
    `script` LONGBLOB COMMENT '脚本内容' ,
    PRIMARY KEY (plugin_id,script_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '插件的前端配置脚本';

CREATE TABLE IF NOT EXISTS plugin_organization
(
    `plugin_id` VARCHAR(50) NOT NULL   COMMENT '插件ID' ,
    `organization_id` VARCHAR(50) NOT NULL   COMMENT '组织ID' ,
    PRIMARY KEY (plugin_id,organization_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '插件和组织的关联表';

CREATE TABLE IF NOT EXISTS schedule
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '',
    `key`         VARCHAR(50) COMMENT 'qrtz UUID',
    `type`        VARCHAR(50)  NOT NULL COMMENT '执行类型 cron',
    `value`       VARCHAR(255) NOT NULL COMMENT 'cron 表达式',
    `job`         VARCHAR(64)  NOT NULL COMMENT 'Schedule Job Class Name',
    `resource_type` VARCHAR(50) NOT NULL DEFAULT 'NONE' COMMENT '资源类型 API_IMPORT,API_SCENARIO,UI_SCENARIO,LOAD_TEST,TEST_PLAN,CLEAN_REPORT,BUG_SYNC' ,
    `enable`      BIT COMMENT '是否开启',
    `resource_id` VARCHAR(50) COMMENT '资源ID，api_scenario ui_scenario load_test',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `project_id`  VARCHAR(50) COMMENT '项目ID',
    `name`        VARCHAR(255) COMMENT '名称',
    `config`      VARCHAR(1000) COMMENT '配置',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时任务';


CREATE INDEX idx_resource_id ON schedule (`resource_id`);
CREATE INDEX idx_create_user ON schedule (`create_user`);
CREATE INDEX idx_create_time ON schedule (`create_time` desc);
CREATE INDEX idx_update_time ON schedule (`update_time` desc);
CREATE INDEX idx_project_id ON schedule (`project_id`);
CREATE INDEX idx_enable ON schedule (`enable`);
CREATE INDEX idx_name ON schedule (`name`);
CREATE INDEX idx_type ON schedule (`type`);
CREATE INDEX idx_resource_type ON schedule(`resource_type`);

CREATE TABLE IF NOT EXISTS service_integration
(
    `id`              VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `plugin_id`       VARCHAR(50) NOT NULL   COMMENT '插件的ID' ,
    `enable`          BIT NOT NULL  DEFAULT 1 COMMENT '是否启用' ,
    `configuration`   BLOB NOT NULL   COMMENT '配置内容' ,
    `organization_id` VARCHAR(50) NOT NULL   COMMENT '组织ID' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '服务集成';


CREATE INDEX idx_organization_id ON service_integration (`organization_id`);

CREATE TABLE IF NOT EXISTS system_parameter
(
    `param_key`   VARCHAR(64)  NOT NULL COMMENT '参数名称',
    `param_value` VARCHAR(255) COMMENT '参数值',
    `type`        VARCHAR(100) NOT NULL DEFAULT 'text' COMMENT '类型',
    PRIMARY KEY (param_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '系统参数';

CREATE TABLE test_resource_pool_blob(
                                        `id` VARCHAR(50) NOT NULL   COMMENT 'id' ,
                                        `configuration` LONGBLOB    COMMENT '资源节点配置' ,
                                        PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '测试资源池大字段';

CREATE TABLE test_resource_pool(
                                   `id` VARCHAR(50) NOT NULL   COMMENT '资源池ID' ,
                                   `name` VARCHAR(255) NOT NULL   COMMENT '名称' ,
                                   `type` VARCHAR(30) NOT NULL   COMMENT '类型' ,
                                   `description` VARCHAR(1000)    COMMENT '描述' ,
                                   `enable` BIT NOT NULL  DEFAULT 1 COMMENT '是否启用' ,
                                   `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                   `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                                   `create_user` VARCHAR(50)    COMMENT '创建人' ,
                                   `api_test` BIT    COMMENT '是否用于接口测试' ,
                                   `load_test` BIT    COMMENT '是否用于性能测试' ,
                                   `ui_test` BIT    COMMENT '是否用于ui测试' ,
                                   `server_url` VARCHAR(255)    COMMENT 'ms部署地址' ,
                                   `all_org` BIT NOT NULL  DEFAULT 1 COMMENT '资源池应用类型（组织/全部）' ,
                                   `deleted` BIT NOT NULL  DEFAULT 0 COMMENT '是否删除' ,
                                   PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '测试资源池';


CREATE INDEX idx_name ON test_resource_pool(`name`);
CREATE INDEX idx_type ON test_resource_pool(`type`);
CREATE INDEX idx_deleted ON test_resource_pool(`deleted`);
CREATE INDEX idx_enable ON test_resource_pool(`enable`);
CREATE INDEX idx_create_time ON test_resource_pool(`create_time` desc);
CREATE INDEX idx_update_time ON test_resource_pool(`update_time` desc);
CREATE INDEX idx_create_user ON test_resource_pool(`create_user`);
CREATE INDEX idx_all_org ON test_resource_pool(`all_org`);

CREATE TABLE IF NOT EXISTS user
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
    `deleted` BIT NOT NULL  DEFAULT 0 COMMENT '是否删除' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户';



CREATE INDEX idx_name ON user (`name`);
CREATE UNIQUE INDEX idx_email ON user (`email`);
CREATE INDEX idx_create_time ON user (`create_time` desc);
CREATE INDEX idx_update_time ON user (`update_time` desc);
CREATE INDEX idx_organization_id ON user (`last_organization_id`);
CREATE INDEX idx_project_id ON user (`last_project_id`);
CREATE INDEX idx_create_user ON user (`create_user`);
CREATE INDEX idx_update_user ON user (`update_user`);
CREATE INDEX idx_deleted ON user (`deleted`);

CREATE TABLE IF NOT EXISTS user_role_relation(
    `id` VARCHAR(50) NOT NULL   COMMENT '用户组关系ID' ,
    `user_id` VARCHAR(50) NOT NULL   COMMENT '用户ID' ,
    `role_id` VARCHAR(50) NOT NULL   COMMENT '组ID' ,
    `source_id` VARCHAR(50) NOT NULL   COMMENT '组织或项目ID' ,
    `organization_id` VARCHAR(50) NOT NULL   COMMENT '记录所在的组织ID' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '用户组关系';



CREATE INDEX idx_user_id ON user_role_relation(`user_id`);
CREATE INDEX idx_group_id ON user_role_relation(`role_id`);
CREATE INDEX idx_source_id ON user_role_relation(`source_id`);
CREATE INDEX idx_create_time ON user_role_relation(`create_time` desc);

CREATE TABLE IF NOT EXISTS user_role_permission
(
    `id`            VARCHAR(64)  NOT NULL COMMENT '',
    `role_id`       VARCHAR(64)  NOT NULL COMMENT '用户组ID',
    `permission_id` VARCHAR(128) NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户组权限';


CREATE INDEX idx_group_id ON user_role_permission (`role_id`);
CREATE INDEX idx_permission_id ON user_role_permission (`permission_id`);

CREATE TABLE user_key
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'user_key ID',
    `create_user` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `access_key`  VARCHAR(50) NOT NULL COMMENT 'access_key',
    `secret_key`  VARCHAR(50) NOT NULL COMMENT 'secret key',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `enable`      BIT         NOT NULL DEFAULT 1 COMMENT '状态',
    `forever`     BIT         NOT NULL DEFAULT 1 COMMENT '是否永久有效',
    `expire_time` BIGINT COMMENT '到期时间',
    `description` VARCHAR(255) COMMENT '描述',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户api key';


CREATE UNIQUE INDEX idx_ak ON user_key (`access_key`);
CREATE INDEX idx_create_user ON user_key (`create_user`);

CREATE TABLE IF NOT EXISTS organization
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '组织ID',
    `num`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '组织编号',
    `name`        VARCHAR(255) NOT NULL COMMENT '组织名称',
    `description` VARCHAR(1000) COMMENT '描述',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user` VARCHAR(50)  NOT NULL COMMENT '修改人',
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
CREATE INDEX idx_create_time ON organization (`create_time` desc);
CREATE INDEX idx_update_time ON organization (`update_time` desc);
CREATE INDEX idx_deleted ON organization (`deleted`);
CREATE INDEX idx_update_user ON organization(`update_user`);

CREATE TABLE IF NOT EXISTS user_extend
(
    `id`            VARCHAR(50) NOT NULL COMMENT '用户ID',
    `platform_info` BLOB COMMENT '其他平台对接信息',
    `avatar`        VARCHAR(255) COMMENT '头像',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户扩展';



CREATE TABLE IF NOT EXISTS test_resource_pool_organization
(
    `id`                    VARCHAR(50) NOT NULL COMMENT '测试资源池项目关系ID',
    `test_resource_pool_id` VARCHAR(50) NOT NULL COMMENT '资源池ID',
    `org_id`                VARCHAR(50) NOT NULL COMMENT '组织ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '测试资源池项目关系';


CREATE INDEX idx_test_resource_pool_id ON test_resource_pool_organization(`test_resource_pool_id`);
CREATE INDEX idx_org_id ON test_resource_pool_organization(`org_id`);


CREATE TABLE IF NOT EXISTS custom_field(
    `id` VARCHAR(50) NOT NULL   COMMENT '自定义字段ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '自定义字段名称' ,
    `scene` VARCHAR(30) NOT NULL   COMMENT '使用场景' ,
    `type` VARCHAR(30) NOT NULL   COMMENT '自定义字段类型' ,
    `remark` VARCHAR(1000)    COMMENT '自定义字段备注' ,
    `internal` BIT NOT NULL  DEFAULT 0 COMMENT '是否是内置字段' ,
    `scope_type` VARCHAR(50) NOT NULL  DEFAULT 0 COMMENT '组织或项目级别字段（PROJECT, ORGANIZATION）' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `ref_id` VARCHAR(50)    COMMENT '项目字段所关联的组织字段ID' ,
    `enable_option_key` BIT   DEFAULT 0 COMMENT '是否需要手动输入选项key' ,
    `scope_id` VARCHAR(50) NOT NULL   COMMENT '组织或项目ID' ,
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '自定义字段';

CREATE INDEX idx_scope_id ON custom_field(scope_id);

CREATE TABLE IF NOT EXISTS custom_field_option(
    `field_id` VARCHAR(50) NOT NULL   COMMENT '自定义字段ID' ,
    `value` VARCHAR(50) NOT NULL   COMMENT '选项值' ,
    `text` VARCHAR(255) NOT NULL   COMMENT '选项值名称' ,
    `internal` BIT NOT NULL  DEFAULT 0 COMMENT '是否内置' ,
    `pos` INT NOT NULL   COMMENT '自定义排序，间隔1' ,
    PRIMARY KEY (field_id,value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '自定义字段选项';

CREATE TABLE IF NOT EXISTS template(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '名称' ,
    `remark` VARCHAR(1000)    COMMENT '备注' ,
    `internal` BIT NOT NULL  DEFAULT 0 COMMENT '是否是内置模板' ,
    `update_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `scope_type` VARCHAR(50) NOT NULL   COMMENT '组织或项目级别字段（PROJECT, ORGANIZATION）' ,
    `scope_id` VARCHAR(50) NOT NULL   COMMENT '组织或项目ID' ,
    `enable_third_part` BIT NOT NULL  DEFAULT 0 COMMENT '是否开启api字段名配置' ,
    `ref_id` VARCHAR(50)    COMMENT '项目模板所关联的组织模板ID' ,
    `scene` VARCHAR(30) NOT NULL   COMMENT '使用场景' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '模版';

CREATE INDEX idx_scope_id_scene ON template(`scope_id`,`scene`);

CREATE TABLE IF NOT EXISTS template_custom_field(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `field_id` VARCHAR(50) NOT NULL   COMMENT '字段ID' ,
    `template_id` VARCHAR(50) NOT NULL   COMMENT '模版ID' ,
    `required` BIT NOT NULL  DEFAULT 0 COMMENT '是否必填' ,
    `system_field` BIT NOT NULL  DEFAULT 0 COMMENT '是否是系统字段' ,
    `pos` INT NOT NULL  DEFAULT 0 COMMENT '排序字段' ,
    `api_field_id` VARCHAR(255)    COMMENT 'api字段名' ,
    `default_value` VARCHAR(1500)    COMMENT '默认值' ,
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci
    COMMENT = '模板和字段的关联关系';

CREATE INDEX idx_template_id ON template_custom_field(template_id);

CREATE TABLE IF NOT EXISTS status_item(
    `id` VARCHAR(50) NOT NULL   COMMENT '状态ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '状态名称' ,
    `scene` VARCHAR(30) NOT NULL   COMMENT '使用场景' ,
    `remark` VARCHAR(1000)    COMMENT '状态说明' ,
    `internal` BIT NOT NULL  DEFAULT 0 COMMENT '是否是内置字段' ,
    `scope_type` VARCHAR(50) NOT NULL  DEFAULT 0 COMMENT '组织或项目级别字段（PROJECT, ORGANIZATION）' ,
    `ref_id` VARCHAR(50)    COMMENT '项目状态所关联的组织状态ID' ,
    `scope_id` VARCHAR(50) NOT NULL   COMMENT '组织或项目ID' ,
    `pos` INT NOT NULL  DEFAULT 0 COMMENT '排序字段' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '状态流的状态项';

CREATE INDEX idx_scope_id ON status_item(scope_id);

CREATE TABLE IF NOT EXISTS status_definition(
  `status_id` VARCHAR(50) NOT NULL   COMMENT '状态ID' ,
  `definition_id` VARCHAR(100) NOT NULL   COMMENT '状态定义ID(在代码中定义)' ,
  PRIMARY KEY (status_id,definition_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '状态定义';

CREATE INDEX idx_status_id ON status_definition(status_id);

CREATE TABLE IF NOT EXISTS status_flow(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `from_id` VARCHAR(50) NOT NULL   COMMENT '起始状态ID' ,
    `to_id` VARCHAR(50) NOT NULL   COMMENT '目的状态ID' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci
    COMMENT = '状态流转';

-- 组织级别参数
CREATE TABLE IF NOT EXISTS organization_parameter(
   `organization_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
   `param_key` VARCHAR(50) NOT NULL   COMMENT '配置项' ,
   `param_value` VARCHAR(255)    COMMENT '配置值' ,
   PRIMARY KEY (organization_id,param_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '组织参数';

-- 用户邀请记录
CREATE TABLE IF NOT EXISTS user_invite
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `email`       VARCHAR(255) NOT NULL COMMENT '邀请邮箱',
    `roles`       TEXT COMMENT '所属权限',
    `invite_user` VARCHAR(50)  NOT NULL COMMENT '邀请用户',
    `invite_time` BIGINT       NOT NULL COMMENT '邀请时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '用户邀请记录';

CREATE TABLE user_local_config
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `user_url`    VARCHAR(50) NOT NULL COMMENT '本地执行程序url',
    `enable`      BIT         NOT NULL DEFAULT 0 COMMENT '本地执行优先',
    `type`        VARCHAR(50) NOT NULL COMMENT 'API/UI',
    `create_user` VARCHAR(50) NOT NULL COMMENT '创建人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '本地执行配置';
CREATE INDEX idx_create_user ON user_local_config(`create_user`);
CREATE INDEX idx_type ON user_local_config(`type`);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;



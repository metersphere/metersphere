-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS custom_function
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '',
    `name`        VARCHAR(255) NOT NULL COMMENT '函数名',
    `tags`        VARCHAR(1000) COMMENT '标签',
    `description` VARCHAR(500) COMMENT '函数描述',
    PRIMARY KEY (id)
)  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '自定义函数-代码片段';


CREATE INDEX name ON custom_function (name);

CREATE TABLE IF NOT EXISTS fake_error
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '误报ID',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '误报名称' ,
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(64)  NOT NULL COMMENT '创建人',
    `update_user` VARCHAR(64)  NOT NULL COMMENT '更新人',
    `type`        VARCHAR(20)  NOT NULL COMMENT '匹配类型/文本内容' ,
    `resp_type`   VARCHAR(20)  NOT NULL COMMENT '响应内容类型/header/data/body' ,
    `relation`    VARCHAR(20)  NOT NULL COMMENT '操作类型/大于/等于/小于' ,
    `expression`  VARCHAR(255) NOT NULL COMMENT '表达式' ,
    `enable`      BIT(1)       NOT NULL DEFAULT 1 COMMENT '启用/禁用' ,
    PRIMARY KEY (id)
)  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '误报库';


CREATE INDEX idx_project_id ON fake_error (project_id);
CREATE INDEX project_id_status ON fake_error (project_id,expression);
CREATE INDEX idx_create_time ON fake_error (create_time);
CREATE INDEX idx_update_time ON fake_error (update_time);
CREATE INDEX idx_create_user ON fake_error (create_user);
CREATE INDEX idx_update_user ON fake_error (update_user);
CREATE INDEX idx_name ON fake_error (name);

CREATE TABLE IF NOT EXISTS file_association
(
    `id`               VARCHAR(50) NOT NULL COMMENT '',
    `type`             VARCHAR(50) NOT NULL COMMENT '模块类型,服务拆分后就是各个服务',
    `source_id`        VARCHAR(50) NOT NULL COMMENT '各个模块关联时自身Id/比如API/CASE/SCENAEIO',
    `source_item_id`   VARCHAR(50) NOT NULL COMMENT '对应资源引用时具体id，如一个用例引用多个文件',
    `file_metadata_id` VARCHAR(50) NOT NULL COMMENT '文件id',
    `file_type`        VARCHAR(50) NOT NULL COMMENT '文件类型',
    `project_id`       VARCHAR(50) NOT NULL COMMENT '项目id',
    PRIMARY KEY (id)
)  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '文件关联资源关系(分散到模块)';


CREATE INDEX idx_file_metadata_id ON file_association (file_metadata_id);
CREATE INDEX idx_project_id ON file_association (project_id);
CREATE INDEX idx_source_id ON file_association (source_id);

CREATE TABLE IF NOT EXISTS file_metadata
(
    `id`            VARCHAR(50)  NOT NULL COMMENT '文件ID',
    `name`          VARCHAR(255) NOT NULL COMMENT '文件名',
    `type`          VARCHAR(64) COMMENT '文件类型',
    `size`          BIGINT       NOT NULL COMMENT '文件大小',
    `create_time`   BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`   BIGINT       NOT NULL COMMENT '更新时间',
    `project_id`    VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `storage`       VARCHAR(50)  NOT NULL DEFAULT 'MINIO' COMMENT '文件存储方式',
    `create_user`   VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user`   VARCHAR(50)  NOT NULL COMMENT '修改人',
    `tags`          VARCHAR(1000) COMMENT '标签',
    `description`   VARCHAR(500) COMMENT '描述',
    `module_id`     VARCHAR(50) COMMENT '文件所属模块',
    `load_jar`      BIT                   DEFAULT 0 COMMENT '是否加载jar（开启后用于接口测试执行时使用）',
    `path`          VARCHAR(1000) COMMENT '文件存储路径',
    `resource_type` VARCHAR(50) COMMENT '资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据',
    `latest`        BIT          NOT NULL DEFAULT 1 COMMENT '是否是最新版',
    `ref_id`        VARCHAR(50)  NOT NULL COMMENT '同版本数据关联的ID',
    PRIMARY KEY (id)
)  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '文件基础信息';


CREATE INDEX idx_file_name ON file_metadata (name);
CREATE INDEX idx_latest ON file_metadata (latest);
CREATE INDEX idx_ref_id ON file_metadata (ref_id);
CREATE INDEX idx_storage ON file_metadata (storage);
CREATE INDEX idx_module_id ON file_metadata (module_id);
CREATE INDEX idx_project_id ON file_metadata (project_id);

CREATE TABLE IF NOT EXISTS file_module
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `project_id`  VARCHAR(50) NOT NULL COMMENT '项目ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '模块名称',
    `parent_id`   VARCHAR(50) COMMENT '父级ID',
    `level`       INT         DEFAULT 1 COMMENT '层数',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `pos`         DOUBLE COMMENT '排序用的标识',
    `create_user` VARCHAR(50) COMMENT '创建人',
    `module_type` VARCHAR(20) DEFAULT 'module' COMMENT '模块类型: module/repository',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci  COMMENT = '文件管理模块';


CREATE INDEX idx_project_id ON file_module (project_id);
CREATE INDEX idx_name ON file_module (name);
CREATE INDEX idx_create_time ON file_module (create_time);
CREATE INDEX idx_update_timed ON file_module (update_time);
CREATE INDEX idx_pos ON file_module (pos);
CREATE INDEX idx_create_user ON file_module (create_user);

CREATE TABLE IF NOT EXISTS project
(
    `id`              VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `num`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目编号',
    `organization_id` VARCHAR(50)  NOT NULL COMMENT '组织ID',
    `name`            VARCHAR(255) NOT NULL COMMENT '项目名称',
    `description`     VARCHAR(500) COMMENT '项目描述',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `update_user`     VARCHAR(50)  NOT NULL COMMENT '修改人',
    `create_user`     VARCHAR(50)  COMMENT '创建人',
    `delete_time`     BIGINT       COMMENT '删除时间',
    `deleted`         BIT          NOT NULL DEFAULT 0 COMMENT '是否删除',
    `delete_user`     VARCHAR(50) COMMENT '删除人',
    `enable`          BIT COMMENT '是否启用',
    `module_setting`  VARCHAR(255)    COMMENT '模块设置' ,
    PRIMARY KEY (id),
    CONSTRAINT idx_num UNIQUE (num)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '项目';


CREATE INDEX idx_organization_id ON project (organization_id);
CREATE INDEX idx_create_user ON project (create_user);
CREATE INDEX idx_create_time ON project (create_time);
CREATE INDEX idx_update_time ON project (update_time);
CREATE INDEX idx_name ON project (name);
CREATE INDEX idx_deleted ON project (deleted);
CREATE INDEX idx_update_user ON project(update_user);

CREATE TABLE IF NOT EXISTS project_application
(
    `project_id` VARCHAR(50) NOT NULL COMMENT '项目ID',
    `type`       VARCHAR(50) NOT NULL COMMENT '配置项',
    `type_value` VARCHAR(255) COMMENT '配置值',
    PRIMARY KEY (project_id, type)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '项目应用';


CREATE INDEX idx_project_application_type ON project_application (type);

CREATE TABLE IF NOT EXISTS project_version
(
    `id`           VARCHAR(50)  NOT NULL COMMENT '版本ID',
    `project_id`   VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `name`         VARCHAR(255) NOT NULL COMMENT '版本名称',
    `description`  VARCHAR(500) COMMENT '描述',
    `status`       VARCHAR(20) COMMENT '状态',
    `latest`       BIT          NOT NULL COMMENT '是否是最新版',
    `publish_time` BIGINT COMMENT '发布时间',
    `start_time`   BIGINT COMMENT '开始时间',
    `end_time`     BIGINT COMMENT '结束时间',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间',
    `create_user`  VARCHAR(50)  NOT NULL COMMENT '创建人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '版本管理';


CREATE INDEX idx_project_id ON project_version (project_id);
CREATE INDEX idx_name ON project_version (name);
CREATE INDEX idx_create_time ON project_version (create_time);
CREATE INDEX idx_create_user ON project_version (create_user);
CREATE INDEX idx_latest ON project_version (latest);

CREATE TABLE IF NOT EXISTS file_module_repository
(
    `file_module_id`       VARCHAR(50) NOT NULL COMMENT 'file_module_id',
    `platform`             VARCHAR(10) COMMENT '所属平台;GitHub/Gitlab/Gitee',
    `repository_path`      VARCHAR(255) COMMENT '存储库地址',
    `repository_user_name` VARCHAR(255) COMMENT '存储库Token;platform为Gitee时必填',
    `repository_token`     VARCHAR(255) COMMENT '存储库Token',
    PRIMARY KEY (file_module_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文件存储库模块';


CREATE TABLE IF NOT EXISTS custom_function_blob
(
    `id`     VARCHAR(50) NOT NULL COMMENT '',
    `params` LONGBLOB COMMENT '参数列表',
    `script` LONGBLOB COMMENT '函数体',
    `result` LONGBLOB COMMENT '执行结果',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '自定义函数-代码片段大字段';

CREATE TABLE IF NOT EXISTS file_metadata_blob
(
    `id`       VARCHAR(50) NOT NULL COMMENT '文件ID',
    `git_info` LONGBLOB COMMENT '储存库',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '文件基础信息大字段';

CREATE TABLE IF NOT EXISTS message_task
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

CREATE TABLE IF NOT EXISTS message_task_blob
(
    `id`       VARCHAR(50) NOT NULL COMMENT '',
    `template` TEXT COMMENT '消息模版',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '消息通知任务大字段';


CREATE TABLE IF NOT EXISTS notification
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


CREATE TABLE IF NOT EXISTS project_robot(
                              `id` VARCHAR(50) NOT NULL   COMMENT 'id' ,
                              `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                              `name` VARCHAR(255) NOT NULL   COMMENT '名称' ,
                              `platform` VARCHAR(50) NOT NULL   COMMENT '所属平台（飞书，钉钉，企业微信，自定义）' ,
                              `webhook` VARCHAR(255) NOT NULL   COMMENT 'webhook' ,
                              `type` VARCHAR(50)    COMMENT '自定义和内部' ,
                              `app_key` VARCHAR(50)    COMMENT '钉钉AppKey' ,
                              `app_secret` VARCHAR(255)    COMMENT '钉钉AppSecret' ,
                              `enable` BIT    COMMENT '是否启用' ,
                              `create_user` VARCHAR(50)    COMMENT '创建人' ,
                              `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                              `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
                              `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                              `description` VARCHAR(255)    COMMENT '描述' ,
                              PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '项目机器人';


CREATE INDEX idx_project_id ON project_robot(project_id);
CREATE INDEX idx_platform ON project_robot(platform);
CREATE INDEX idx_webhook ON project_robot(webhook);


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
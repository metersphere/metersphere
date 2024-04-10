-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS custom_function
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '主键ID',
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `name`        VARCHAR(255) NOT NULL COMMENT '函数名',
    `tags`        VARCHAR(1000) COMMENT '标签',
    `description` VARCHAR(1000) COMMENT '函数描述',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '脚本语言类型',
    `status` VARCHAR(50)    COMMENT '脚本状态（草稿/测试通过）' ,
    `create_time` BIGINT    COMMENT '创建时间' ,
    `update_time` BIGINT    COMMENT '更新时间' ,
    `create_user` VARCHAR(50)    COMMENT '创建人' ,
    `update_user` VARCHAR(50)    COMMENT '更新人' ,
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
CREATE INDEX idx_create_time ON fake_error (create_time desc);
CREATE INDEX idx_update_time ON fake_error (update_time desc);
CREATE INDEX idx_create_user ON fake_error (create_user);
CREATE INDEX idx_update_user ON fake_error (update_user);
CREATE INDEX idx_name ON fake_error (name);

CREATE TABLE IF NOT EXISTS file_association
(
       `id` VARCHAR(50) NOT NULL   COMMENT '' ,
       `source_type` VARCHAR(50) NOT NULL   COMMENT '资源类型' ,
       `source_id` VARCHAR(50) NOT NULL   COMMENT '资源ID' ,
       `file_id` VARCHAR(50) NOT NULL   COMMENT '文件ID' ,
       `file_ref_id` VARCHAR(50) NOT NULL   COMMENT '文件同版本ID' ,
       `file_version` VARCHAR(50) NOT NULL   COMMENT '文件版本' ,
       `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
       `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
       `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
       `create_user` VARCHAR(50)    COMMENT '创建人' ,
       `deleted` BIT NOT NULL  DEFAULT 0 COMMENT '是否删除' ,
       `deleted_file_name` VARCHAR(255)    COMMENT '删除时的文件名称' ,
       PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文件资源关联';




CREATE INDEX idx_file_metadata_id ON file_association (file_id);
CREATE INDEX idx_source_type ON file_association (source_type);
CREATE INDEX idx_source_id ON file_association (source_id);


CREATE TABLE IF NOT EXISTS file_metadata_repository
(
    `file_metadata_id` VARCHAR(50)   NOT NULL COMMENT '文件ID',
    `branch`           VARCHAR(255)  NOT NULL COMMENT '分支',
    `commit_id`        VARCHAR(255) COMMENT '提交ID',
    `commit_message`   TEXT COMMENT '提交信息',
    PRIMARY KEY (file_metadata_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '存储库文件信息拓展';


CREATE TABLE IF NOT EXISTS file_metadata
(
    `id`           VARCHAR(50)  NOT NULL COMMENT '文件ID',
    `name`         VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255)    COMMENT '原始名（含后缀）' ,
    `type`         VARCHAR(64) COMMENT '文件类型',
    `size`         BIGINT       NOT NULL COMMENT '文件大小',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`  BIGINT       NOT NULL COMMENT '更新时间',
    `project_id`   VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `storage`      VARCHAR(50)  NOT NULL DEFAULT 'MINIO' COMMENT '文件存储方式',
    `create_user`  VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user`  VARCHAR(50)  NOT NULL COMMENT '修改人',
    `tags`         VARCHAR(1000) COMMENT '标签',
    `description`  VARCHAR(1000) COMMENT '描述',
    `module_id`    VARCHAR(50) COMMENT '文件所属模块',
    `path`         VARCHAR(1000) COMMENT '文件存储路径',
    `latest`       BIT          NOT NULL DEFAULT 1 COMMENT '是否是最新版',
    `enable` BIT NOT NULL DEFAULT 0 COMMENT '启用/禁用;启用禁用（一般常用于jar文件）',
    `ref_id`       VARCHAR(50)  NOT NULL COMMENT '同版本数据关联的ID',
    `file_version` VARCHAR(50) COMMENT '文件版本号',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文件基础信息';


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
    `name`        VARCHAR(255) NOT NULL COMMENT '模块名称',
    `parent_id`   VARCHAR(50) COMMENT '父级ID',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `pos` BIGINT NOT NULL DEFAULT 0 COMMENT '排序用的标识',
    `update_user` VARCHAR(50) COMMENT '修改人',
    `create_user` VARCHAR(50) COMMENT '创建人',
    `module_type` VARCHAR(20) DEFAULT 'module' COMMENT '模块类型: module/repository',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文件管理模块';


CREATE INDEX idx_project_id ON file_module (project_id);
CREATE INDEX idx_name ON file_module (name);
CREATE INDEX idx_create_time ON file_module (create_time desc);
CREATE INDEX idx_update_timed ON file_module (update_time desc);
CREATE INDEX idx_pos ON file_module (pos);
CREATE INDEX idx_create_user ON file_module (create_user);
CREATE UNIQUE INDEX uq_name_project_parent_type ON file_module (project_id, name, module_type, parent_id);


CREATE TABLE IF NOT EXISTS project
(
    `id`              VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `num`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目编号',
    `organization_id` VARCHAR(50)  NOT NULL COMMENT '组织ID',
    `name`            VARCHAR(255) NOT NULL COMMENT '项目名称',
    `description`     VARCHAR(1000) COMMENT '项目描述',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `update_user`     VARCHAR(50)  NOT NULL COMMENT '修改人',
    `create_user`     VARCHAR(50)  COMMENT '创建人',
    `delete_time`     BIGINT       COMMENT '删除时间',
    `deleted`         BIT          NOT NULL DEFAULT 0 COMMENT '是否删除',
    `delete_user`     VARCHAR(50) COMMENT '删除人',
    `enable`          BIT          NOT NULL DEFAULT 1 COMMENT '是否启用',
    `module_setting`  VARCHAR(255)    COMMENT '模块设置' ,
    PRIMARY KEY (id),
    CONSTRAINT idx_num UNIQUE (num)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '项目';


CREATE INDEX idx_organization_id ON project (organization_id);
CREATE INDEX idx_create_user ON project (create_user);
CREATE INDEX idx_create_time ON project (create_time desc);
CREATE INDEX idx_update_time ON project (update_time desc);
CREATE INDEX idx_name ON project (name);
CREATE INDEX idx_deleted ON project (deleted);
CREATE INDEX idx_update_user ON project(update_user);

CREATE TABLE IF NOT EXISTS project_application
(
    `project_id` VARCHAR(50) NOT NULL COMMENT '项目ID',
    `type`       VARCHAR(50) NOT NULL COMMENT '配置项',
    `type_value` VARCHAR(512) COMMENT '配置值',
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
    `description`  VARCHAR(1000) COMMENT '描述',
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
CREATE INDEX idx_create_time ON project_version (create_time desc);
CREATE INDEX idx_create_user ON project_version (create_user);
CREATE INDEX idx_latest ON project_version (latest);

CREATE TABLE IF NOT EXISTS file_module_repository
(
    `file_module_id` VARCHAR(50)  NOT NULL COMMENT 'file_module_id',
    `platform`       VARCHAR(10)  NOT NULL COMMENT '所属平台;GitHub/Gitlab/Gitee',
    `url`            VARCHAR(255) NOT NULL COMMENT '存储库地址',
    `token`          VARCHAR(255) NOT NULL COMMENT '存储库Token',
    `user_name`      VARCHAR(255) COMMENT '用户名;platform为Gitee时必填',
    PRIMARY KEY (file_module_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文件存储库模块';

CREATE INDEX idx_token ON file_module_repository (token);


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

CREATE TABLE IF NOT EXISTS message_task(
       `id` VARCHAR(50) NOT NULL   COMMENT '' ,
       `event` VARCHAR(255) NOT NULL   COMMENT '通知事件类型' ,
       `receivers` VARCHAR(1000) NOT NULL   COMMENT '接收人id集合' ,
       `project_robot_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '机器人id' ,
       `task_type` VARCHAR(64) NOT NULL   COMMENT '任务类型' ,
       `test_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '具体测试的ID' ,
       `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
       `enable` BIT NOT NULL  DEFAULT 0 COMMENT '是否启用' ,
       `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
       `create_time` BIGINT NOT NULL  DEFAULT 0 COMMENT '创建时间' ,
       `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
       `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
       `use_default_template` BIT NOT NULL  DEFAULT 1 COMMENT '是否使用默认模版' ,
       `use_default_subject` BIT NOT NULL  DEFAULT 1 COMMENT '是否使用默认标题（仅邮件）' ,
       `subject` VARCHAR(1000)    COMMENT '邮件标题' ,
       PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '消息通知任务';


CREATE INDEX idx_project_id ON message_task(project_id);
CREATE INDEX idx_create_time ON message_task(create_time desc);
CREATE INDEX idx_test_id ON message_task(test_id);
CREATE INDEX idx_task_type ON message_task(task_type);
CREATE INDEX idx_project_robot_id ON message_task(project_robot_id);
CREATE INDEX idx_event ON message_task(event);
CREATE INDEX idx_enable ON message_task(enable);
CREATE INDEX idx_use_default_subject ON message_task(use_default_subject);
CREATE INDEX idx_use_default_template ON message_task(use_default_template);

CREATE TABLE IF NOT EXISTS message_task_blob
(
    `id`       VARCHAR(50) NOT NULL COMMENT '',
    `template` TEXT COMMENT '消息模版',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '消息通知任务大字段';

CREATE TABLE IF NOT EXISTS notification(
                                           `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT 'ID' ,
                                           `type` VARCHAR(64) NOT NULL   COMMENT '通知类型' ,
                                           `receiver` VARCHAR(50) NOT NULL   COMMENT '接收人' ,
                                           `subject` VARCHAR(255) NOT NULL   COMMENT '标题' ,
                                           `status` VARCHAR(64) NOT NULL   COMMENT '状态' ,
                                           `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                           `operator` VARCHAR(50) NOT NULL   COMMENT '操作人' ,
                                           `operation` VARCHAR(50) NOT NULL   COMMENT '操作' ,
                                           `resource_id` VARCHAR(50) NOT NULL   COMMENT '资源ID' ,
                                           `project_id` VARCHAR(50) NOT NULL   COMMENT '项目id' ,
                                           `organization_id` VARCHAR(50) NOT NULL   COMMENT '组织id' ,
                                           `resource_type` VARCHAR(64) NOT NULL   COMMENT '资源类型' ,
                                           `resource_name` VARCHAR(255) NOT NULL   COMMENT '资源名称' ,
                                           `content` TEXT NOT NULL   COMMENT '通知内容' ,
                                           PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '消息通知';


CREATE INDEX idx_receiver ON notification(receiver);
CREATE INDEX idx_create_time ON notification(create_time);
CREATE INDEX idx_type ON notification(type);
CREATE INDEX idx_subject ON notification(subject);
CREATE INDEX idx_resource_type ON notification(resource_type);
CREATE INDEX idx_operator ON notification(operator);
CREATE INDEX idx_resource_id ON notification(resource_id);
CREATE INDEX idx_project_id ON notification(project_id);
CREATE INDEX idx_organization_id ON notification(organization_id);


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
                              `description` VARCHAR(1000)    COMMENT '描述' ,
                              PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '项目机器人';


CREATE INDEX idx_project_id ON project_robot(project_id);
CREATE INDEX idx_platform ON project_robot(platform);
CREATE INDEX idx_webhook ON project_robot(webhook);


CREATE TABLE IF NOT EXISTS project_test_resource_pool(
                                `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                `test_resource_pool_id` VARCHAR(50) NOT NULL   COMMENT '资源池ID' ,
                                 PRIMARY KEY (project_id,test_resource_pool_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '项目与资源池关系表';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
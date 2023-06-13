-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

DROP TABLE IF EXISTS custom_field;
CREATE TABLE custom_field
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '自定义字段ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '自定义字段名称',
    `scene`       VARCHAR(30)  NOT NULL COMMENT '使用场景',
    `type`        VARCHAR(30)  NOT NULL COMMENT '自定义字段类型',
    `remark`      VARCHAR(255) COMMENT '自定义字段备注',
    `options`     TEXT COMMENT '自定义字段选项',
    `system`      BIT                   DEFAULT 0 COMMENT '是否是系统字段',
    `global`      BIT                   DEFAULT 0 COMMENT '是否是全局字段',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50) COMMENT '创建人',
    `project_id`  VARCHAR(50) COMMENT '项目ID',
    `third_part`  BIT          NOT NULL DEFAULT 0 COMMENT '是否关联第三方',
    PRIMARY KEY (id)
) COMMENT = '自定义字段';


CREATE INDEX idx_global ON custom_field (global);
CREATE INDEX idx_name ON custom_field (name);
CREATE INDEX idx_scene ON custom_field (scene);
CREATE INDEX idx_create_time ON custom_field (create_time);
CREATE INDEX idx_update_time ON custom_field (update_time);
CREATE INDEX idx_create_user ON custom_field (create_user);
CREATE INDEX idx_project_id ON custom_field (project_id);

DROP TABLE IF EXISTS custom_field_template;
CREATE TABLE custom_field_template
(
    `id`            VARCHAR(50) NOT NULL COMMENT '自定义模版ID',
    `field_id`      VARCHAR(50) NOT NULL COMMENT '自定义字段ID',
    `template_id`   VARCHAR(50) NOT NULL COMMENT '模版ID',
    `scene`         VARCHAR(30) NOT NULL COMMENT '使用场景',
    `required`      BIT COMMENT '是否必填',
    `pos`           INT COMMENT '排序字段',
    `default_value` LONGBLOB COMMENT '默认值',
    `custom_data`   VARCHAR(255) COMMENT '自定义数据',
    `key`           VARCHAR(1) COMMENT '自定义表头',
    PRIMARY KEY (id)
) COMMENT = '自定义模版';


CREATE INDEX custom_field_template_field_id_index ON custom_field_template (field_id);
CREATE INDEX custom_field_template_template_id_index ON custom_field_template (template_id);

DROP TABLE IF EXISTS custom_function;
CREATE TABLE custom_function
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '',
    `name`        VARCHAR(255) NOT NULL COMMENT '函数名',
    `tags`        VARCHAR(1000) COMMENT '标签',
    `description` VARCHAR(500) COMMENT '函数描述',
    PRIMARY KEY (id)
) COMMENT = '自定义函数-代码片段';


CREATE INDEX name ON custom_function (name);

DROP TABLE IF EXISTS fake_error;
CREATE TABLE fake_error
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '误报ID',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(64)  NOT NULL COMMENT '创建人',
    `update_user` VARCHAR(64)  NOT NULL COMMENT '更新人',
    `error_code`  VARCHAR(255) NOT NULL COMMENT '错误码',
    `match_type`  VARCHAR(255) NOT NULL COMMENT '匹配类型',
    `status`      BIT COMMENT '状态',
    PRIMARY KEY (id)
) COMMENT = '误报库';


CREATE INDEX idx_project_id ON fake_error (project_id);
CREATE INDEX project_id_status ON fake_error (project_id, status);
CREATE INDEX idx_create_time ON fake_error (create_time);
CREATE INDEX idx_update_time ON fake_error (update_time);
CREATE INDEX idx_create_user ON fake_error (create_user);
CREATE INDEX idx_update_user ON fake_error (update_user);

DROP TABLE IF EXISTS file_association;
CREATE TABLE file_association
(
    `id`               VARCHAR(50) NOT NULL COMMENT '',
    `type`             VARCHAR(50) NOT NULL COMMENT '模块类型,服务拆分后就是各个服务',
    `source_id`        VARCHAR(50) NOT NULL COMMENT '各个模块关联时自身Id/比如API/CASE/SCENAEIO',
    `source_item_id`   VARCHAR(50) NOT NULL COMMENT '对应资源引用时具体id，如一个用例引用多个文件',
    `file_metadata_id` VARCHAR(50) NOT NULL COMMENT '文件id',
    `file_type`        VARCHAR(50) NOT NULL COMMENT '文件类型',
    `project_id`       VARCHAR(50) NOT NULL COMMENT '项目id',
    PRIMARY KEY (id)
) COMMENT = '文件关联资源关系(分散到模块)';


CREATE INDEX idx_file_metadata_id ON file_association (file_metadata_id);
CREATE INDEX idx_project_id ON file_association (project_id);
CREATE INDEX idx_source_id ON file_association (source_id);

DROP TABLE IF EXISTS file_metadata;
CREATE TABLE file_metadata
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
) COMMENT = '文件基础信息';


CREATE INDEX idx_file_name ON file_metadata (name);
CREATE INDEX idx_latest ON file_metadata (latest);
CREATE INDEX idx_ref_id ON file_metadata (ref_id);
CREATE INDEX idx_storage ON file_metadata (storage);
CREATE INDEX idx_module_id ON file_metadata (module_id);
CREATE INDEX idx_project_id ON file_metadata (project_id);

DROP TABLE IF EXISTS file_module;
CREATE TABLE file_module
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
) COMMENT = '文件管理模块';


CREATE INDEX idx_project_id ON file_module (project_id);
CREATE INDEX idx_name ON file_module (name);
CREATE INDEX idx_create_time ON file_module (create_time);
CREATE INDEX idx_update_timed ON file_module (update_time);
CREATE INDEX idx_pos ON file_module (pos);
CREATE INDEX idx_create_user ON file_module (create_user);

DROP TABLE IF EXISTS project;
CREATE TABLE project
(
    `id`              VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `num`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目编号',
    `organization_id` VARCHAR(50)  NOT NULL COMMENT '组织ID',
    `name`            VARCHAR(255) NOT NULL COMMENT '项目名称',
    `description`     VARCHAR(500) COMMENT '项目描述',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `update_user`     VARCHAR(50)  NOT NULL COMMENT '修改人',
    `create_user`     VARCHAR(50) COMMENT '创建人',
    `delete_time`     BIGINT(255) COMMENT '删除时间',
    `deleted`         BIT          NOT NULL DEFAULT 0 COMMENT '是否删除',
    `delete_user`     VARCHAR(50) COMMENT '删除人',
    `enable`          BIT COMMENT '是否启用',
    PRIMARY KEY (id),
    CONSTRAINT idx_num UNIQUE (num)
) COMMENT = '项目';


CREATE INDEX idx_organization_id ON project (organization_id);
CREATE INDEX idx_create_user ON project (create_user);
CREATE INDEX idx_create_time ON project (create_time);
CREATE INDEX idx_update_time ON project (update_time);
CREATE INDEX idx_name ON project (name);
CREATE INDEX idx_deleted ON project (deleted);
CREATE INDEX idx_update_user ON project(update_user);

DROP TABLE IF EXISTS project_application;
CREATE TABLE project_application
(
    `project_id` VARCHAR(50) NOT NULL COMMENT '项目ID',
    `type`       VARCHAR(50) NOT NULL COMMENT '配置项',
    `type_value` VARCHAR(255) COMMENT '配置值',
    PRIMARY KEY (project_id, type)
) COMMENT = '项目应用';


CREATE INDEX idx_project_application_type ON project_application (type);

DROP TABLE IF EXISTS project_version;
CREATE TABLE project_version
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
) COMMENT = '版本管理';


CREATE INDEX idx_project_id ON project_version (project_id);
CREATE INDEX idx_name ON project_version (name);
CREATE INDEX idx_create_time ON project_version (create_time);
CREATE INDEX idx_create_user ON project_version (create_user);
CREATE INDEX idx_latest ON project_version (latest);

DROP TABLE IF EXISTS file_module_blob;
CREATE TABLE file_module_blob
(
    `id`                   VARCHAR(50) NOT NULL COMMENT 'ID',
    `repository_desc`      LONGBLOB COMMENT '存储库描述',
    `repository_path`      VARCHAR(255) COMMENT '存储库路径',
    `repository_user_name` VARCHAR(255) COMMENT '存储库Token',
    `repository_token`     VARCHAR(255) COMMENT '存储库Token',
    PRIMARY KEY (id)
) COMMENT = '文件管理模块大字段';

DROP TABLE IF EXISTS custom_function_blob;
CREATE TABLE custom_function_blob
(
    `id`     VARCHAR(50) NOT NULL COMMENT '',
    `params` LONGBLOB COMMENT '参数列表',
    `script` LONGBLOB COMMENT '函数体',
    `result` LONGBLOB COMMENT '执行结果',
    PRIMARY KEY (id)
) COMMENT = '自定义函数-代码片段大字段';

DROP TABLE IF EXISTS fake_error_blob;
CREATE TABLE fake_error_blob
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'Test ID',
    `content`     LONGBLOB COMMENT '内容',
    `description` LONGBLOB COMMENT '报告内容',
    PRIMARY KEY (id)
) COMMENT = '误报库大字段';

DROP TABLE IF EXISTS file_metadata_blob;
CREATE TABLE file_metadata_blob
(
    `id`       VARCHAR(50) NOT NULL COMMENT '文件ID',
    `git_info` LONGBLOB COMMENT '储存库',
    PRIMARY KEY (id)
) COMMENT = '文件基础信息大字段';

DROP TABLE IF EXISTS project_extend;
CREATE TABLE project_extend
(
    `id`                  VARCHAR(50) NOT NULL COMMENT '项目ID',
    `tapd_id`             VARCHAR(50) COMMENT '',
    `jira_key`            VARCHAR(50) COMMENT '',
    `zentao_id`           VARCHAR(50) COMMENT '',
    `azure_devops_id`     VARCHAR(50) COMMENT '',
    `case_template_id`    VARCHAR(50) COMMENT '用例模版ID',
    `azure_filter_id`     VARCHAR(50) COMMENT 'azure 过滤需求的 parent workItem ID',
    `platform`            VARCHAR(20) NOT NULL DEFAULT 'Local' COMMENT '项目使用哪个平台的模板',
    `third_part_template` BIT                  DEFAULT 0 COMMENT '是否使用第三方平台缺陷模板',
    `version_enable`      BIT                  DEFAULT 1 COMMENT '是否开启版本管理',
    `issue_config`        VARCHAR(2000) COMMENT '',
    `api_template_id`     VARCHAR(64) COMMENT '',
    PRIMARY KEY (id)
) COMMENT = '项目扩展';


CREATE INDEX idx_project_id ON project_extend (id);

DROP TABLE IF EXISTS functional_case_template;
CREATE TABLE functional_case_template
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `description` VARCHAR(500) COMMENT '描述',
    `internal`    BIT          NOT NULL DEFAULT 0 COMMENT '是否是内置模板',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (id)
) COMMENT = '功能用例模版';


CREATE INDEX idx_name ON functional_case_template (name);
CREATE INDEX idx_system ON functional_case_template (internal);
CREATE INDEX idx_create_time ON functional_case_template (create_time);
CREATE INDEX idx_create_user ON functional_case_template (create_user);
CREATE INDEX idx_project_id ON functional_case_template (project_id);

DROP TABLE IF EXISTS functional_case_template_extend;
CREATE TABLE functional_case_template_extend
(
    `id`               VARCHAR(50) NOT NULL COMMENT '模板ID',
    `case_name`        VARCHAR(255) COMMENT '用例名称模板',
    `prerequisite`     TEXT COMMENT '前置条件模板',
    `step_description` TEXT COMMENT '步骤描述模板',
    `expected_result`  TEXT COMMENT '预期结果模板',
    `actual_result`    TEXT COMMENT '实际结果模板',
    `step_model`       VARCHAR(64) NOT NULL DEFAULT 'Step' COMMENT '编辑模式模板：步骤模式/文本模式',
    `steps`            TEXT COMMENT '用例步骤',
    PRIMARY KEY (id)
) COMMENT = '功能用例模版扩展';

DROP TABLE IF EXISTS bug_template_extend;
CREATE TABLE bug_template_extend
(
    `id`      VARCHAR(50) NOT NULL COMMENT '缺陷模板ID',
    `title`   VARCHAR(255) COMMENT '缺陷标题模板',
    `content` TEXT COMMENT '缺陷内容模板',
    PRIMARY KEY (id)
) COMMENT = '缺陷模板扩展';

DROP TABLE IF EXISTS bug_template;
CREATE TABLE bug_template
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `description` VARCHAR(500) COMMENT '描述',
    `internal`    BIT          NOT NULL DEFAULT 0 COMMENT '是否是内置模板',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (id)
) COMMENT = '缺陷模版';


CREATE INDEX idx_name ON bug_template (name);
CREATE INDEX idx_system ON bug_template (internal);
CREATE INDEX idx_create_time ON bug_template (create_time);
CREATE INDEX idx_create_user ON bug_template (create_user);
CREATE INDEX idx_project_id ON bug_template (project_id);

DROP TABLE IF EXISTS api_template;
CREATE TABLE api_template
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `description` VARCHAR(500) COMMENT '描述',
    `internal`    BIT          NOT NULL DEFAULT 0 COMMENT '是否是内置模板',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    PRIMARY KEY (id)
) COMMENT = '接口定义模板';


CREATE INDEX idx_name ON api_template (name);
CREATE INDEX idx_system ON api_template (internal);
CREATE INDEX idx_create_time ON api_template (create_time);
CREATE INDEX idx_create_user ON api_template (create_user);
CREATE INDEX idx_project_id ON api_template (project_id);


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
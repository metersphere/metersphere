-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS api_debug(
    `id` VARCHAR(50) NOT NULL   COMMENT '接口pk' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '接口名称' ,
    `protocol` VARCHAR(20) NOT NULL   COMMENT '接口协议' ,
    `method` VARCHAR(20)    COMMENT 'http协议类型post/get/其它协议则是协议名(mqtt)' ,
    `path` VARCHAR(500)    COMMENT 'http协议路径/其它协议则为空' ,
    `pos` BIGINT NOT NULL  DEFAULT 0 COMMENT '自定义排序' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
    `module_id` VARCHAR(50) NOT NULL  DEFAULT 'root' COMMENT '模块fk' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `update_time` BIGINT NOT NULL   COMMENT '修改时间' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口调试';

CREATE INDEX idx_project_id ON api_debug(project_id);
CREATE INDEX idx_module_id ON api_debug(module_id);
CREATE INDEX idx_protocol ON api_debug(protocol);
CREATE INDEX idx_create_time ON api_debug(create_time desc);
CREATE INDEX idx_create_user ON api_debug(create_user);
CREATE INDEX idx_name ON api_debug(name);


CREATE TABLE IF NOT EXISTS api_debug_blob(
    `id` VARCHAR(50) NOT NULL   COMMENT '接口fk/ 一对一关系' ,
    `request` LONGBLOB    COMMENT '请求内容' ,
    `response` LONGBLOB    COMMENT '响应内容' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口调试详情内容';

CREATE TABLE IF NOT EXISTS api_debug_module(
    `id` VARCHAR(50) NOT NULL   COMMENT '接口模块pk' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '模块名称' ,
    `protocol` VARCHAR(20) NOT NULL   COMMENT '协议' ,
    `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父级fk' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
    `pos` BIGINT NOT NULL   COMMENT '排序' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '修改时间' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口调试模块';

CREATE INDEX idx_project_id ON api_debug_module(project_id);
CREATE INDEX idx_protocol ON api_debug_module(protocol);
CREATE INDEX idx_pos ON api_debug_module(pos);
CREATE INDEX idx_create_user ON api_debug_module(create_user);


CREATE TABLE IF NOT EXISTS api_definition
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '接口pk',
    `name`        VARCHAR(255) NOT NULL COMMENT '接口名称',
    `protocol`    VARCHAR(20)  NOT NULL COMMENT '接口协议',
    `method`      VARCHAR(20) COMMENT 'http协议类型post/get/其它协议则是协议名(mqtt)',
    `path`        VARCHAR(500) COMMENT 'http协议路径/其它协议则为空',
    `status`      VARCHAR(50)  NOT NULL COMMENT '接口状态/进行中/已完成',
    `num`         BIGINT       NOT NULL COMMENT '自定义id',
    `tags`        VARCHAR(500) COMMENT '标签',
    `pos`         BIGINT       NOT NULL COMMENT '自定义排序',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目fk',
    `module_id`   VARCHAR(50)  NOT NULL DEFAULT 'root' COMMENT '模块fk',
    `latest`      BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是',
    `version_id`  VARCHAR(50)  NOT NULL COMMENT '版本fk',
    `ref_id`      VARCHAR(50)  NOT NULL COMMENT '版本引用fk',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_time` BIGINT       NOT NULL COMMENT '修改时间',
    `update_user` VARCHAR(50)  NOT NULL COMMENT '修改人',
    `delete_user` VARCHAR(50) COMMENT '删除人',
    `delete_time` BIGINT COMMENT '删除时间',
    `deleted`     BIT(1)       NOT NULL DEFAULT 0 COMMENT '删除状态',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口定义';


CREATE INDEX idx_project_id ON api_definition(project_id);
CREATE INDEX idx_module_id ON api_definition(module_id);
CREATE INDEX idx_ref_id ON api_definition(ref_id);
CREATE INDEX idx_version_id ON api_definition(version_id);
CREATE INDEX idx_status ON api_definition(status);
CREATE INDEX idx_pos ON api_definition(pos);
CREATE INDEX idx_protocol ON api_definition(protocol);
CREATE INDEX idx_create_time ON api_definition(create_time desc);
CREATE INDEX idx_create_user ON api_definition(create_user);
CREATE INDEX idx_name ON api_definition(name);

CREATE TABLE IF NOT EXISTS api_report(
  `id` VARCHAR(50) NOT NULL   COMMENT '接口结果报告pk' ,
  `name` VARCHAR(200) NOT NULL   COMMENT '接口报告名称' ,
  `resource_id` VARCHAR(50) NOT NULL   COMMENT '资源fk/api_definition_id/api_test_case_id' ,
  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
  `update_time` BIGINT NOT NULL   COMMENT '修改时间' ,
  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人fk' ,
  `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
  `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '删除状态' ,
  `status` VARCHAR(50) NOT NULL  DEFAULT 'Pending' COMMENT '结果状态' ,
  `start_time` BIGINT    COMMENT '接口开始执行时间' ,
  `end_time` BIGINT    COMMENT '接口执行结束时间' ,
  `run_mode` VARCHAR(20) NOT NULL   COMMENT '执行模块/API/CASE/API_PLAN' ,
  `pool_id` VARCHAR(50) NOT NULL   COMMENT '资源池fk' ,
  `trigger_mode` VARCHAR(50) NOT NULL   COMMENT '触发模式/手动/批量/定时任务/JENKINS' ,
  `version_id` VARCHAR(50)    COMMENT '版本fk' ,
  `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
  `integrated_report_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '集成报告id/api_scenario_report_id' ,
  `integrated` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否为集成报告，默认否' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci  COMMENT = 'API/CASE执行结果';


CREATE INDEX idx_integrated ON api_report(integrated);
CREATE INDEX idx_update_time ON api_report(update_time desc);
CREATE INDEX idx_project_id ON api_report(project_id);
CREATE INDEX idx_resource_id ON api_report(resource_id);
CREATE INDEX idx_status ON api_report(status);
CREATE INDEX idx_create_user ON api_report(create_user);

CREATE TABLE IF NOT EXISTS api_definition_follower(
  `api_definition_id` VARCHAR(50) NOT NULL   COMMENT '接口fk' ,
  `user_id` VARCHAR(50) NOT NULL   COMMENT '关注人/用户fk' ,
  PRIMARY KEY (api_definition_id,user_id)
)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_general_ci COMMENT = '接口定义关注人';

CREATE TABLE IF NOT EXISTS api_definition_module(
  `id` VARCHAR(50) NOT NULL   COMMENT '接口模块pk' ,
  `name` VARCHAR(255) NOT NULL   COMMENT '模块名称' ,
  `protocol` VARCHAR(20) NOT NULL   COMMENT '协议' ,
  `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父级fk' ,
  `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
  `pos` INT NOT NULL   COMMENT '排序' ,
  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
  `update_time` BIGINT NOT NULL   COMMENT '修改时间' ,
  `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口模块';


CREATE INDEX idx_project_id ON api_definition_module(project_id);
CREATE INDEX idx_protocol ON api_definition_module(protocol);
CREATE INDEX idx_pos ON api_definition_module(pos);

CREATE TABLE IF NOT EXISTS api_scenario(
    `id` VARCHAR(50) NOT NULL   COMMENT '' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '场景名称' ,
    `level` VARCHAR(10) NOT NULL   COMMENT '场景级别/P0/P1等' ,
    `status` VARCHAR(20) NOT NULL   COMMENT '场景状态/未规划/已完成 等' ,
    `principal` VARCHAR(50) NOT NULL   COMMENT '责任人/用户fk' ,
    `step_total` INT NOT NULL  DEFAULT 0 COMMENT '场景步骤总数' ,
    `pass_rate` BIGINT NOT NULL  DEFAULT 0 COMMENT '通过率' ,
    `last_report_status` VARCHAR(50)    COMMENT '最后一次执行的结果状态' ,
    `last_report_id` VARCHAR(50)    COMMENT '最后一次执行的报告fk' ,
    `num` INT    COMMENT '编号' ,
    `custom_num` VARCHAR(50)    COMMENT '自定义id' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '删除状态' ,
    `pos` BIGINT NOT NULL   COMMENT '自定义排序' ,
    `version_id` VARCHAR(50)    COMMENT '版本fk' ,
    `ref_id` VARCHAR(50)    COMMENT '引用资源fk' ,
    `latest` BIT(1)   DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
    `api_scenario_module_id` VARCHAR(50)    COMMENT '场景模块fk' ,
    `description` VARCHAR(500)    COMMENT '描述信息' ,
    `tags` VARCHAR(1000)    COMMENT '标签' ,
    `grouped` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否为环境组' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `delete_time` BIGINT    COMMENT '删除时间' ,
    `delete_user` VARCHAR(50)    COMMENT '删除人' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '更新人' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
     PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景';


CREATE INDEX idx_api_scenario_module_id ON api_scenario(api_scenario_module_id);
CREATE INDEX idx_ref_id ON api_scenario(ref_id);
CREATE INDEX idx_version_id ON api_scenario(version_id);
CREATE INDEX idx_project_id ON api_scenario(project_id);
CREATE INDEX idx_status ON api_scenario(status);
CREATE INDEX idx_report_status ON api_scenario(last_report_status);
CREATE INDEX idx_create_time ON api_scenario(create_time desc);
CREATE INDEX idx_update_time ON api_scenario(update_time desc);
CREATE INDEX idx_create_user ON api_scenario(create_user);
CREATE INDEX idx_num ON api_scenario(num);
CREATE INDEX idx_level ON api_scenario(level);
CREATE INDEX idx_name ON api_scenario(name);

CREATE TABLE IF NOT EXISTS api_scenario_follower(
  `api_scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景fk' ,
  `user_id` VARCHAR(50) NOT NULL   COMMENT '关注人/用户fk' ,
  PRIMARY KEY (api_scenario_id,user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '关注记录';


CREATE INDEX uk_api_scenario_id_follow_id ON api_scenario_follower(api_scenario_id,user_id);

CREATE TABLE IF NOT EXISTS api_scenario_module(
  `id` VARCHAR(50) NOT NULL   COMMENT '场景模块pk' ,
  `name` VARCHAR(255) NOT NULL   COMMENT '模块名称' ,
  `level` INT NOT NULL  DEFAULT 1 COMMENT '模块级别' ,
  `pos` INT NOT NULL   COMMENT '排序' ,
  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
  `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
  `update_user` VARCHAR(50) NOT NULL   COMMENT '更新人' ,
  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
  `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
  `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父级fk' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景模块';


CREATE INDEX idx_project_id ON api_scenario_module(project_id);

CREATE TABLE IF NOT EXISTS api_scenario_reference(
  `id` VARCHAR(50) NOT NULL   COMMENT '引用关系pk' ,
  `api_scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景fk' ,
  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
  `reference_id` VARCHAR(50) NOT NULL   COMMENT '引用步骤fk' ,
  `reference_type` VARCHAR(20)    COMMENT '引用步骤类型/REF/COPY' ,
  `data_type` VARCHAR(20)    COMMENT '步骤类型/CASE/API' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景步骤引用CASE关系记录';


CREATE INDEX idx_api_scenario_id ON api_scenario_reference(api_scenario_id);
CREATE INDEX idx_reference_id ON api_scenario_reference(reference_id);
CREATE INDEX idx_create_user ON api_scenario_reference(create_user);
CREATE INDEX idx_reference_type ON api_scenario_reference(reference_type);

CREATE TABLE IF NOT EXISTS api_scenario_report(
  `id` VARCHAR(50) NOT NULL   COMMENT '场景报告pk' ,
  `name` VARCHAR(255) NOT NULL   COMMENT '报告名称' ,
  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
  `delete_time` BIGINT    COMMENT '删除时间' ,
  `delete_user` VARCHAR(50)    COMMENT '删除人' ,
  `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '删除标识' ,
  `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
  `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
  `pass_rate` BIGINT    COMMENT '通过率' ,
  `status` VARCHAR(20) NOT NULL  DEFAULT 'Pending' COMMENT '报告状态/SUCCESS/ERROR' ,
  `trigger_mode` VARCHAR(20) NOT NULL   COMMENT '触发方式' ,
  `run_mode` VARCHAR(20) NOT NULL   COMMENT '执行模式' ,
  `pool_id` VARCHAR(50) NOT NULL   COMMENT '资源池' ,
  `version_id` VARCHAR(50)    COMMENT '版本fk' ,
  `integrated` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是集成报告' ,
  `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
  `scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景fk' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景报告';


CREATE INDEX idx_project_id ON api_scenario_report(project_id);
CREATE INDEX idx_scenario_id ON api_scenario_report(scenario_id);
CREATE INDEX idx_trigger_mode ON api_scenario_report(trigger_mode);
CREATE INDEX idx_run_mode ON api_scenario_report(run_mode);
CREATE INDEX idx_status ON api_scenario_report(status);
CREATE INDEX idx_update_time ON api_scenario_report(update_time desc);
CREATE INDEX idx_create_user ON api_scenario_report(create_user);
CREATE INDEX idx_name ON api_scenario_report(name);
CREATE INDEX idx_pool_id ON api_scenario_report(pool_id);

CREATE TABLE IF NOT EXISTS api_scenario_report_detail(
  `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
  `report_id` VARCHAR(50) NOT NULL   COMMENT '报告fk' ,
  `resource_id` VARCHAR(50) NOT NULL   COMMENT '场景中各个步骤请求唯一标识' ,
  `start_time` BIGINT    COMMENT '开始时间' ,
  `status` VARCHAR(20)    COMMENT '结果状态' ,
  `request_time` BIGINT    COMMENT '单个请求步骤时间' ,
  `assertions_total` BIGINT    COMMENT '总断言数' ,
  `pass_assertions_total` BIGINT    COMMENT '失败断言数' ,
  `fake_code` VARCHAR(200)    COMMENT '误报编号' ,
  `request_name` VARCHAR(500)    COMMENT '请求名称' ,
  `environment_id` VARCHAR(50)    COMMENT '环境fk' ,
  `project_id` VARCHAR(50)    COMMENT '项目fk' ,
  `error_total` INT    COMMENT '失败总数' ,
  `code` VARCHAR(500)    COMMENT '请求响应码' ,
  `content` LONGBLOB    COMMENT '执行结果' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景报告步骤结果';


CREATE INDEX idx_report_id ON api_scenario_report_detail(report_id);
CREATE INDEX idx_resource_id ON api_scenario_report_detail(resource_id);
CREATE INDEX idx_project_id ON api_scenario_report_detail(project_id);

CREATE TABLE IF NOT EXISTS api_scenario_report_structure(
                                              `report_id` VARCHAR(50) NOT NULL   COMMENT '请求资源 id' ,
                                              `resource_tree` LONGBLOB    COMMENT '资源步骤结构树' ,
                                              PRIMARY KEY (report_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景报告结构';

CREATE TABLE IF NOT EXISTS api_sync_config(
  `id` VARCHAR(50) NOT NULL   COMMENT '' ,
  `resource_id` VARCHAR(50) NOT NULL   COMMENT 'API/CASE 来源fk' ,
  `resource_type` VARCHAR(50) NOT NULL   COMMENT '来源类型/API/CASE' ,
  `hide` BIT(1)   DEFAULT 0 COMMENT '是否隐藏' ,
  `rule_config` LONGTEXT    COMMENT '同步规则' ,
  `notify_case_creator` BIT(1) NOT NULL  DEFAULT 1 COMMENT '是否通知用例创建人' ,
  `notify_scenario_creator` BIT(1) NOT NULL  DEFAULT 1 COMMENT '是否通知场景创建人' ,
  `sync_enable` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否同步用例' ,
  `notice_enable` BIT(1)   DEFAULT 0 COMMENT '是否发送通知' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口同步用例配置';


CREATE INDEX idx_resource_id ON api_sync_config(resource_id);

CREATE TABLE IF NOT EXISTS api_test_case
(
    `id`                 VARCHAR(50)  NOT NULL COMMENT '接口用例pk',
    `name`               VARCHAR(255) NOT NULL COMMENT '接口用例名称',
    `priority`           VARCHAR(50)  NOT NULL COMMENT '用例等级',
    `num`                BIGINT       NOT NULL COMMENT '接口用例编号id',
    `tags`               VARCHAR(1000) COMMENT '标签',
    `status`             VARCHAR(20)  NOT NULL COMMENT '用例状态',
    `last_report_status` VARCHAR(20) COMMENT '最新执行结果状态',
    `last_report_id`     VARCHAR(50) COMMENT '最后执行结果报告fk',
    `pos`                BIGINT       NOT NULL COMMENT '自定义排序',
    `project_id`         VARCHAR(50)  NOT NULL COMMENT '项目fk',
    `api_definition_id`  VARCHAR(50)  NOT NULL COMMENT '接口fk',
    `version_id`         VARCHAR(50)  NOT NULL COMMENT '版本fk',
    `environment_id`     VARCHAR(50) COMMENT '环境fk',
    `create_time`        BIGINT       NOT NULL COMMENT '创建时间',
    `create_user`        VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_time`        BIGINT       NOT NULL COMMENT '更新时间',
    `update_user`        VARCHAR(50)  NOT NULL COMMENT '更新人',
    `delete_time`        BIGINT COMMENT '删除时间',
    `delete_user`        VARCHAR(50) COMMENT '删除人',
    `deleted`            BIT(1)       NOT NULL DEFAULT 0 COMMENT '删除标识',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口用例';


CREATE INDEX idx_api_definition_id ON api_test_case(api_definition_id);
CREATE INDEX idx_project_id ON api_test_case(project_id);
CREATE INDEX idx_status ON api_test_case(status);
CREATE INDEX idx_version_id ON api_test_case(version_id);
CREATE INDEX idx_priority ON api_test_case(priority);
CREATE INDEX idx_create_time ON api_test_case(create_time desc);
CREATE INDEX idx_update_time ON api_test_case(update_time desc);
CREATE INDEX idx_create_user ON api_test_case(create_user);
CREATE INDEX idx_name ON api_test_case(name);
CREATE INDEX idx_num ON api_test_case(num);

CREATE TABLE IF NOT EXISTS api_test_case_follower(
  `case_id` VARCHAR(50) NOT NULL   COMMENT '用例fk' ,
  `user_id` VARCHAR(50) NOT NULL   COMMENT '关注人/用户fk' ,
  PRIMARY KEY (case_id,user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口用例关注人';

CREATE TABLE IF NOT EXISTS api_definition_mock(
    `id` VARCHAR(50) NOT NULL   COMMENT 'mock pk' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '修改时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `name` VARCHAR(200) NOT NULL   COMMENT 'mock名称' ,
    `tags` VARCHAR(500)    COMMENT '自定义标签' ,
    `enable` BIT(1) NOT NULL  DEFAULT 1 COMMENT '启用/禁用' ,
    `expect_num` VARCHAR(50) NOT NULL   COMMENT 'mock编号' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
    `api_definition_id` VARCHAR(50) NOT NULL   COMMENT '接口fk' ,
    PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'mock配置';


CREATE INDEX idx_api_definition_id ON api_definition_mock(api_definition_id);
CREATE INDEX idx_project_id ON api_definition_mock(project_id);

CREATE TABLE IF NOT EXISTS api_definition_mock_config(
    `id` VARCHAR(50) NOT NULL   COMMENT 'mock pk' ,
    `matching` LONGBLOB    COMMENT '匹配规则' ,
    `response` LONGBLOB    COMMENT '响应内容' ,
    PRIMARY KEY (id)
)  ENGINE = InnoDB
   DEFAULT CHARSET = utf8mb4
   COLLATE = utf8mb4_general_ci
   COMMENT = 'mock期望值配置';

CREATE TABLE IF NOT EXISTS api_definition_swagger(
  `id` VARCHAR(50) NOT NULL   COMMENT '主键' ,
  `swagger_url` VARCHAR(500) NOT NULL   COMMENT 'url地址' ,
  `module_id` VARCHAR(50)    COMMENT '模块fk' ,
  `module_path` VARCHAR(1000)    COMMENT '模块路径' ,
  `config` BLOB    COMMENT '鉴权配置信息' ,
  `mode` BIT(1)   DEFAULT 0 COMMENT '导入模式/覆盖/不覆盖' ,
  `project_id` VARCHAR(50)    COMMENT '项目fk' ,
  `version_id` VARCHAR(50)    COMMENT '导入版本' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时同步配置';


CREATE INDEX idx_project_id ON api_definition_swagger(project_id);

CREATE TABLE IF NOT EXISTS api_definition_blob(
  `id` VARCHAR(50) NOT NULL   COMMENT '接口fk/ 一对一关系' ,
  `request` LONGBLOB    COMMENT '请求内容' ,
  `response` LONGBLOB    COMMENT '响应内容' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口定义详情内容';

CREATE TABLE IF NOT EXISTS api_report_blob(
  `id` VARCHAR(50) NOT NULL   COMMENT '接口报告fk' ,
  `content` LONGBLOB    COMMENT '结果内容详情' ,
  `config` BLOB    COMMENT '执行环境配置' ,
  `console` BLOB    COMMENT '执行过程日志' ,
  PRIMARY KEY (id)
)  ENGINE = InnoDB
   DEFAULT CHARSET = utf8mb4
   COLLATE = utf8mb4_general_ci COMMENT = 'API/CASE执行结果详情';

CREATE TABLE IF NOT EXISTS api_scenario_blob(
  `id` VARCHAR(50) NOT NULL   COMMENT '场景pk' ,
  `content` LONGBLOB    COMMENT '场景步骤内容' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景步骤详情';

CREATE TABLE IF NOT EXISTS api_test_case_blob(
  `id` VARCHAR(50) NOT NULL   COMMENT '接口用例pk' ,
  `request` LONGBLOB    COMMENT '请求内容' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口用例详情';

CREATE TABLE IF NOT EXISTS api_scenario_report_log(
  `report_id` VARCHAR(50) NOT NULL   COMMENT '请求资源 id' ,
  `console` LONGBLOB    COMMENT '执行日志' ,
  PRIMARY KEY (report_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景报告过程日志';

CREATE TABLE IF NOT EXISTS api_scenario_environment(
  `id` VARCHAR(50) NOT NULL   COMMENT '场景环境pk' ,
  `api_scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景fk' ,
  `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
  `environment_id` VARCHAR(50)    COMMENT '环境fk' ,
  `environment_group_id` VARCHAR(50)    COMMENT '环境组fk' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景环境';


CREATE INDEX idx_api_scenario_id ON api_scenario_environment(api_scenario_id);
CREATE INDEX idx_project_id ON api_scenario_environment(project_id);
CREATE INDEX idx_environment_id ON api_scenario_environment(environment_id);


CREATE TABLE IF NOT EXISTS api_file_resource(
    `resource_id` VARCHAR(50) NOT NULL   COMMENT '资源ID(接口用例等)' ,
    `file_id` VARCHAR(50) NOT NULL   COMMENT '文件ID' ,
    `file_name` VARCHAR(50) NOT NULL   COMMENT '文件名称' ,
    `resource_type` VARCHAR(50) NOT NULL   COMMENT '资源类型(API_DEBUG,API,API_CASE,API_SCENARIO)' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    PRIMARY KEY (resource_id,file_id)
)  COMMENT = '接口和所需文件资源的关联表';


CREATE TABLE IF NOT EXISTS api_definition_custom_field(
    `api_id` VARCHAR(50) NOT NULL   COMMENT '接口ID' ,
    `field_id` VARCHAR(50) NOT NULL   COMMENT '字段ID' ,
    `value` VARCHAR(1000)    COMMENT '字段值' ,
    PRIMARY KEY (api_id,field_id)
)  COMMENT = '自定义字段接口定义关系';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
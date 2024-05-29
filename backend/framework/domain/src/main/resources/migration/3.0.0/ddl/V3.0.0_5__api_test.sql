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
CREATE INDEX idx_pos ON api_debug_module(pos);
CREATE INDEX idx_create_user ON api_debug_module(create_user);
CREATE UNIQUE INDEX uq_name_project_parent_type ON api_debug_module (project_id, name, parent_id);


CREATE TABLE IF NOT EXISTS api_definition
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '接口pk',
    `name`        VARCHAR(255) NOT NULL COMMENT '接口名称',
    `protocol`    VARCHAR(20)  NOT NULL COMMENT '接口协议',
    `method`      VARCHAR(20) COMMENT 'http协议类型post/get/其它协议则是协议名(mqtt)',
    `path`        VARCHAR(500) COMMENT 'http协议路径/其它协议则为空',
    `status`      VARCHAR(50)  NOT NULL COMMENT '接口状态/进行中/已完成',
    `num`         BIGINT       NOT NULL COMMENT '自定义id',
    `tags`        VARCHAR(1000) COMMENT '标签',
    `pos`         BIGINT       NOT NULL COMMENT '自定义排序',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目fk',
    `module_id`   VARCHAR(50)  NOT NULL DEFAULT 'root' COMMENT '模块fk',
    `latest`      BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是',
    `version_id`  VARCHAR(50)  NOT NULL COMMENT '版本fk',
    `ref_id`      VARCHAR(50)  NOT NULL COMMENT '版本引用fk',
    `description` VARCHAR(1000) COMMENT '描述',
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
    `id` VARCHAR(50) NOT NULL   COMMENT '接口报告pk' ,
    `name` VARCHAR(300) NOT NULL   COMMENT '接口报告名称' ,
    `test_plan_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '测试计划id' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `delete_time` BIGINT    COMMENT '删除时间' ,
    `delete_user` VARCHAR(50)    COMMENT '删除人' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '删除标识' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `start_time` BIGINT NOT NULL   COMMENT '开始时间/同创建时间一致' ,
    `end_time` BIGINT    COMMENT '结束时间/报告执行完成' ,
    `request_duration` BIGINT NOT NULL  DEFAULT 0 COMMENT '请求总耗时' ,
    `status` VARCHAR(20) NOT NULL  DEFAULT 'PENDING' COMMENT '报告状态/SUCCESS/ERROR' ,
    `trigger_mode` VARCHAR(20) NOT NULL   COMMENT '触发方式' ,
    `run_mode` VARCHAR(20) NOT NULL   COMMENT '执行模式' ,
    `pool_id` VARCHAR(50) NOT NULL   COMMENT '资源池' ,
    `integrated` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是集成报告' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
    `environment_id` VARCHAR(50)    COMMENT '环境' ,
    `error_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '失败数' ,
    `fake_error_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '误报数' ,
    `pending_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '未执行数' ,
    `success_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '成功数' ,
    `assertion_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '总断言数' ,
    `assertion_success_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '成功断言数' ,
    `request_error_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求失败率' ,
    `request_pending_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求未执行率' ,
    `request_fake_error_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求误报率' ,
    `request_pass_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求通过率' ,
    `assertion_pass_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '断言通过率' ,
    `script_identifier` VARCHAR(255)    COMMENT '脚本标识' ,
    PRIMARY KEY (id)
)
    ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci  COMMENT = 'API/CASE执行结果';

CREATE INDEX idx_project_id ON api_report(project_id);
CREATE INDEX idx_trigger_mode ON api_report(trigger_mode);
CREATE INDEX idx_run_mode ON api_report(run_mode);
CREATE INDEX idx_status ON api_report(status);
CREATE INDEX idx_update_time ON api_report(update_time);
CREATE INDEX idx_create_user ON api_report(create_user);
CREATE INDEX idx_name ON api_report(name);
CREATE INDEX idx_pool_id ON api_report(pool_id);
CREATE INDEX idx_start_time ON api_report(start_time);
CREATE INDEX idx_integrated ON api_report(integrated);
CREATE INDEX idx_test_plan_id ON api_report(test_plan_id);

CREATE TABLE IF NOT EXISTS api_report_step(
    `step_id` VARCHAR(50) NOT NULL   COMMENT '步骤id' ,
    `report_id` VARCHAR(50) NOT NULL   COMMENT '报告id' ,
    `name` VARCHAR(255)    COMMENT '步骤名称' ,
    `sort` BIGINT NOT NULL   COMMENT '序号' ,
    `step_type` VARCHAR(50) NOT NULL   COMMENT '步骤类型/API/CASE等' ,
    PRIMARY KEY (step_id,report_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'API报告步骤';

CREATE INDEX idx_report_id ON api_report_step(report_id);
CREATE INDEX idx_sort ON api_report_step(sort);
CREATE INDEX idx_step_type ON api_report_step(step_type);

CREATE TABLE IF NOT EXISTS api_report_detail(
    `id` VARCHAR(50) NOT NULL   COMMENT '报告详情id' ,
    `report_id` VARCHAR(50) NOT NULL   COMMENT '接口报告id' ,
    `step_id` VARCHAR(50) NOT NULL   COMMENT '各个步骤请求唯一标识' ,
    `status` VARCHAR(20) NOT NULL   COMMENT '结果状态' ,
    `fake_code` VARCHAR(200)    COMMENT '误报编号/误报状态独有' ,
    `request_name` VARCHAR(500)    COMMENT '请求名称' ,
    `request_time` BIGINT NOT NULL   COMMENT '请求耗时' ,
    `code` VARCHAR(500)    COMMENT '请求响应码' ,
    `response_size` BIGINT NOT NULL   COMMENT '响应内容大小' ,
    `content` LONGBLOB    COMMENT '结果内容详情' ,
    `script_identifier` VARCHAR(255)    COMMENT '脚本标识' ,
    PRIMARY KEY (id)
)   ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci  COMMENT = 'API/CASE执行结果详情';

CREATE INDEX idx_report ON api_report_detail(report_id);
CREATE INDEX idx_step_id ON api_report_detail(step_id);

CREATE TABLE api_report_log(
    `id` VARCHAR(50) NOT NULL   COMMENT '主键' ,
    `report_id` VARCHAR(50) NOT NULL   COMMENT '请求资源 id' ,
    `console` LONGBLOB    COMMENT '执行日志' ,
    PRIMARY KEY (id)
)   ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci  COMMENT = '接口报告过程日志';

CREATE INDEX idx_report_id ON api_report_log(report_id);

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
CREATE INDEX idx_pos ON api_definition_module(pos);
CREATE UNIQUE INDEX uq_name_project_parent_type ON api_definition_module (project_id, name, parent_id);

CREATE TABLE IF NOT EXISTS api_scenario(
    `id` VARCHAR(50) NOT NULL   COMMENT 'id' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '场景名称' ,
    `priority` VARCHAR(10) NOT NULL   COMMENT '场景级别/P0/P1等' ,
    `status` VARCHAR(20) NOT NULL   COMMENT '场景状态/未规划/已完成 等' ,
    `step_total` INT NOT NULL  DEFAULT 0 COMMENT '场景步骤总数' ,
    `request_pass_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求通过率' ,
    `last_report_status` VARCHAR(50)  NOT NULL  DEFAULT 'PENDING'  COMMENT '最后一次执行的结果状态' ,
    `last_report_id` VARCHAR(50)    COMMENT '最后一次执行的报告fk' ,
    `num` BIGINT NOT NULL   COMMENT '编号' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '删除状态' ,
    `pos` BIGINT NOT NULL   COMMENT '自定义排序' ,
    `version_id` VARCHAR(50) NOT NULL   COMMENT '版本fk' ,
    `ref_id` VARCHAR(50) NOT NULL   COMMENT '引用资源fk' ,
    `latest` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
    `module_id` VARCHAR(50) NOT NULL   COMMENT '场景模块fk' ,
    `description` VARCHAR(1000)    COMMENT '描述信息' ,
    `tags` VARCHAR(1000)    COMMENT '标签' ,
    `grouped` BIT(1)   DEFAULT 0 COMMENT '是否为环境组' ,
    `environment_id` VARCHAR(50)    COMMENT '环境或者环境组ID' ,
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

CREATE INDEX idx_module_id ON api_scenario(module_id);
CREATE INDEX idx_ref_id ON api_scenario(ref_id);
CREATE INDEX idx_version_id ON api_scenario(version_id);
CREATE INDEX idx_project_id ON api_scenario(project_id);
CREATE INDEX idx_status ON api_scenario(status);
CREATE INDEX idx_report_status ON api_scenario(last_report_status);
CREATE INDEX idx_create_time ON api_scenario(create_time desc);
CREATE INDEX idx_update_time ON api_scenario(update_time desc);
CREATE INDEX idx_create_user ON api_scenario(create_user);
CREATE INDEX idx_num ON api_scenario(num);
CREATE INDEX idx_priority ON api_scenario(priority);
CREATE INDEX idx_name ON api_scenario(name);

CREATE TABLE IF NOT EXISTS api_scenario_step(
    `id` VARCHAR(50) NOT NULL   COMMENT '步骤id' ,
    `scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景id' ,
    `name` VARCHAR(255)    COMMENT '步骤名称' ,
    `sort` BIGINT NOT NULL   COMMENT '序号' ,
    `enable` BIT(1) NOT NULL  DEFAULT 1 COMMENT '启用/禁用' ,
    `resource_id` VARCHAR(50)    COMMENT '资源id' ,
    `resource_num` VARCHAR(50)    COMMENT '资源编号' ,
    `step_type` VARCHAR(50)    COMMENT '步骤类型/API/CASE等' ,
    `project_id` VARCHAR(50)    COMMENT '项目fk' ,
    `parent_id` VARCHAR(50)   DEFAULT 'NONE' COMMENT '父级fk' ,
    `version_id` VARCHAR(50)    COMMENT '版本号' ,
    `ref_type` VARCHAR(20)    COMMENT '引用/复制/自定义' ,
    `origin_project_id` VARCHAR(50) COMMENT '记录跨项目复制的步骤的原项目ID' ,
    `config` VARCHAR(500)    COMMENT '循环等组件基础数据' ,
    PRIMARY KEY (id)
)  ENGINE = InnoDB
   DEFAULT CHARSET = utf8mb4
   COLLATE = utf8mb4_general_ci COMMENT = '场景步骤';

CREATE INDEX idx_project_id ON api_scenario_step(project_id);
CREATE INDEX idx_sort ON api_scenario_step(sort);
CREATE INDEX idx_resource_id ON api_scenario_step(resource_id);
CREATE INDEX idx_enable ON api_scenario_step(enable);
CREATE INDEX idx_resource_num ON api_scenario_step(resource_num);

CREATE TABLE IF NOT EXISTS api_scenario_step_blob(
    `id` VARCHAR(50) NOT NULL   COMMENT '场景步骤id' ,
    `scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景id' ,
    `content` LONGBLOB    COMMENT '场景步骤内容' ,
    PRIMARY KEY (id)
)  ENGINE = InnoDB
   DEFAULT CHARSET = utf8mb4
   COLLATE = utf8mb4_general_ci COMMENT = '场景步骤内容';

CREATE INDEX idx_scenario_id ON api_scenario_step_blob(scenario_id);


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
  `pos` BIGINT NOT NULL   COMMENT '排序' ,
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
CREATE INDEX idx_pos ON api_scenario_module(pos);
CREATE UNIQUE INDEX uq_name_project_parent_type ON api_scenario_module (project_id, name, parent_id);

CREATE TABLE IF NOT EXISTS  api_scenario_report(
    `id` VARCHAR(50) NOT NULL   COMMENT '场景报告pk' ,
    `name` VARCHAR(300) NOT NULL   COMMENT '报告名称' ,
    `test_plan_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '测试计划id' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `delete_time` BIGINT    COMMENT '删除时间' ,
    `delete_user` VARCHAR(50)    COMMENT '删除人' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '删除标识' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `start_time` BIGINT NOT NULL   COMMENT '开始时间/同创建时间一致' ,
    `end_time` BIGINT    COMMENT '结束时间/报告执行完成' ,
    `request_duration` BIGINT NOT NULL  DEFAULT 0 COMMENT '请求总耗时' ,
    `status` VARCHAR(20) NOT NULL  DEFAULT 'PENDING' COMMENT '报告状态/SUCCESS/ERROR' ,
    `trigger_mode` VARCHAR(20) NOT NULL   COMMENT '触发方式' ,
    `run_mode` VARCHAR(20) NOT NULL   COMMENT '执行模式' ,
    `pool_id` VARCHAR(50) NOT NULL   COMMENT '资源池' ,
    `integrated` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是集成报告' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
    `environment_id` VARCHAR(50)    COMMENT '环境' ,
    `error_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '失败数' ,
    `fake_error_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '误报数' ,
    `pending_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '未执行数' ,
    `success_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '成功数' ,
    `assertion_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '总断言数' ,
    `assertion_success_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '成功断言数' ,
    `request_error_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求失败率' ,
    `request_pending_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求未执行率' ,
    `request_fake_error_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求误报率' ,
    `request_pass_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '请求通过率' ,
    `assertion_pass_rate` VARCHAR(20) NOT NULL  DEFAULT 'Calculating' COMMENT '断言通过率' ,
    `script_identifier` VARCHAR(255)    COMMENT '脚本标识' ,
    `waiting_time` BIGINT    COMMENT '等待时间' ,
    PRIMARY KEY (id)
)   ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '场景报告';

CREATE INDEX idx_project_id ON api_scenario_report(project_id);
CREATE INDEX idx_trigger_mode ON api_scenario_report(trigger_mode);
CREATE INDEX idx_run_mode ON api_scenario_report(run_mode);
CREATE INDEX idx_status ON api_scenario_report(status);
CREATE INDEX idx_update_time ON api_scenario_report(update_time);
CREATE INDEX idx_create_user ON api_scenario_report(create_user);
CREATE INDEX idx_name ON api_scenario_report(name);
CREATE INDEX idx_pool_id ON api_scenario_report(pool_id);
CREATE INDEX idx_integrated ON api_scenario_report(integrated);
CREATE INDEX idx_start_time ON api_scenario_report(start_time);
CREATE INDEX idx_test_plan_id ON api_scenario_report(test_plan_id);

CREATE TABLE IF NOT EXISTS api_scenario_report_detail(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `report_id` VARCHAR(50) NOT NULL   COMMENT '报告fk' ,
    `step_id` VARCHAR(50) NOT NULL   COMMENT '场景中各个步骤请求唯一标识' ,
    `status` VARCHAR(20)    COMMENT '结果状态' ,
    `fake_code` VARCHAR(200)    COMMENT '误报编号/误报状态独有' ,
    `request_name` VARCHAR(500)    COMMENT '请求名称' ,
    `request_time` BIGINT NOT NULL   COMMENT '请求耗时' ,
    `code` VARCHAR(500)    COMMENT '请求响应码' ,
    `response_size` BIGINT NOT NULL  DEFAULT 0 COMMENT '响应内容大小' ,
    `script_identifier` VARCHAR(255)    COMMENT '脚本标识' ,
    `content` LONGBLOB    COMMENT '执行结果' ,
    `sort` BIGINT NOT NULL   COMMENT '用于循环请求排序' ,

    PRIMARY KEY (id)
)   ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '场景报告步骤结果';

CREATE INDEX idx_report_id ON api_scenario_report_detail(report_id);
CREATE INDEX idx_resource_id ON api_scenario_report_detail(step_id);

CREATE TABLE IF NOT EXISTS api_scenario_report_step(
    `step_id` VARCHAR(50) NOT NULL   COMMENT '步骤id' ,
    `report_id` VARCHAR(50) NOT NULL   COMMENT '请求资源 id' ,
    `name` VARCHAR(255)    COMMENT '步骤名称' ,
    `sort` BIGINT NOT NULL   COMMENT '序号' ,
    `step_type` VARCHAR(50) NOT NULL   COMMENT '步骤类型/API/CASE等' ,
    `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父级fk' ,
    PRIMARY KEY (step_id,report_id)
)  ENGINE = InnoDB
   DEFAULT CHARSET = utf8mb4
   COLLATE = utf8mb4_general_ci COMMENT = '场景报告步骤';

CREATE INDEX idx_sort ON api_scenario_report_step(sort);
CREATE INDEX idx_parent_id ON api_scenario_report_step(parent_id);

CREATE TABLE IF NOT EXISTS api_scenario_report_log(
    `id` VARCHAR(50) NOT NULL   COMMENT '主键' ,
    `report_id` VARCHAR(50) NOT NULL   COMMENT '请求资源 id' ,
    `console` LONGBLOB    COMMENT '执行日志' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景报告过程日志';

CREATE INDEX idx_report_id ON api_scenario_report_log(report_id);


CREATE TABLE IF NOT EXISTS api_test_case
(
    `id`                 VARCHAR(50)  NOT NULL COMMENT '接口用例pk',
    `name`               VARCHAR(255) NOT NULL COMMENT '接口用例名称',
    `priority`           VARCHAR(50)  NOT NULL COMMENT '用例等级',
    `num`                BIGINT       NOT NULL COMMENT '接口用例编号id',
    `tags`               VARCHAR(1000) COMMENT '标签',
    `status`             VARCHAR(20)  NOT NULL COMMENT '用例状态',
    `last_report_status` VARCHAR(20) NOT NULL  DEFAULT 'PENDING' COMMENT '最新执行结果状态',
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
    `name` VARCHAR(255) NOT NULL   COMMENT 'mock名称' ,
    `tags` VARCHAR(1000)    COMMENT '自定义标签' ,
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
    `num` BIGINT NOT NULL   COMMENT '业务id' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '定时任务名称' ,
    `swagger_url` VARCHAR(500) NOT NULL   COMMENT 'url地址' ,
    `module_id` VARCHAR(50)    COMMENT '模块fk' ,
    `config` VARCHAR(255)    COMMENT '鉴权配置信息' ,
    `cover_data` BIT(1)   DEFAULT 0 COMMENT '导入模式/覆盖/不覆盖' ,
    `cover_module` BIT(1)   DEFAULT 0 COMMENT '是否覆盖模块' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
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


CREATE TABLE IF NOT EXISTS api_scenario_blob(
  `id` VARCHAR(50) NOT NULL  COMMENT '场景pk' ,
  `config` LONGBLOB    COMMENT '场景配置信息' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景配置信息等详情';

CREATE TABLE IF NOT EXISTS api_test_case_blob(
  `id` VARCHAR(50) NOT NULL   COMMENT '接口用例pk' ,
  `request` LONGBLOB    COMMENT '请求内容' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口用例详情';

CREATE TABLE IF NOT EXISTS api_file_resource(
    `resource_id` VARCHAR(50) NOT NULL   COMMENT '资源ID(接口用例等)' ,
    `file_id` VARCHAR(50) NOT NULL   COMMENT '文件ID' ,
    `file_name` VARCHAR(255) NOT NULL   COMMENT '文件名称' ,
    `resource_type` VARCHAR(50) NOT NULL   COMMENT '资源类型(API_DEBUG,API,API_CASE,API_SCENARIO)' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    PRIMARY KEY (resource_id,file_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '接口和所需文件资源的关联表';


CREATE TABLE IF NOT EXISTS api_definition_custom_field(
    `api_id` VARCHAR(50) NOT NULL   COMMENT '接口ID' ,
    `field_id` VARCHAR(50) NOT NULL   COMMENT '字段ID' ,
    `value` VARCHAR(1000)    COMMENT '字段值' ,
    PRIMARY KEY (api_id,field_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '自定义字段接口定义关系';

CREATE TABLE api_scenario_csv
(
    `id`                 VARCHAR(50)  NOT NULL COMMENT 'id',
    `file_id`            VARCHAR(50)  NOT NULL COMMENT '文件id/引用文件id',
    `scenario_id`        VARCHAR(50)  NOT NULL COMMENT '场景id',
    `name`               VARCHAR(255) NOT NULL COMMENT 'csv变量名称',
    `file_name`          VARCHAR(255) COMMENT '文件名称',
    `scope`              VARCHAR(50)  NOT NULL COMMENT '作用域 SCENARIO/STEP',
    `enable`             BIT(1)       NOT NULL DEFAULT 1 COMMENT '启用/禁用',
    `association`        BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否引用',
    `encoding`           VARCHAR(50)  NOT NULL DEFAULT 'UTF-8' COMMENT '文件编码',
    `random`             BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否随机',
    `variable_names`     VARCHAR(255) COMMENT '变量名称(西文逗号间隔)',
    `ignore_first_line`  BIT(1)       NOT NULL DEFAULT 0 COMMENT '忽略首行(只有在设置了变量名称后才生效)',
    `delimiter`          VARCHAR(50) COMMENT '分隔符',
    `allow_quoted_data`  BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否允许带引号',
    `recycle_on_eof`     BIT(1)       NOT NULL DEFAULT 1 COMMENT '遇到文件结束符再次循环',
    `stop_thread_on_eof` BIT(1)       NOT NULL DEFAULT 0 COMMENT '遇到文件结束符停止线程',
    `project_id`         VARCHAR(50)  NOT NULL COMMENT '项目id',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景csv';

CREATE INDEX idx_scenario_id ON api_scenario_csv(scenario_id);
CREATE INDEX idx_name ON api_scenario_csv(name);
CREATE INDEX idx_file_name ON api_scenario_csv(file_name);
CREATE INDEX idx_project_id ON api_scenario_csv(project_id);

CREATE TABLE api_scenario_csv_step
(
    `id`      VARCHAR(50) NOT NULL COMMENT 'id',
    `file_id` VARCHAR(50) NOT NULL COMMENT '文件id',
    `step_id` VARCHAR(50) NOT NULL COMMENT '步骤id',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景csv引用关系';

CREATE INDEX idx_file_id ON api_scenario_csv_step(file_id);
CREATE INDEX idx_step_id ON api_scenario_csv_step(step_id);

CREATE TABLE  IF NOT EXISTS api_scenario_record(
    `api_scenario_report_id` VARCHAR(50) NOT NULL   COMMENT '报告id' ,
    `api_scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景id' ,
    PRIMARY KEY (api_scenario_report_id,api_scenario_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '场景执行记录';


CREATE TABLE  IF NOT EXISTS api_test_case_record(
    `api_report_id` VARCHAR(50) NOT NULL   COMMENT '报告id' ,
    `api_test_case_id` VARCHAR(50) NOT NULL   COMMENT '用例id' ,
    PRIMARY KEY (api_report_id,api_test_case_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用例执行记录';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
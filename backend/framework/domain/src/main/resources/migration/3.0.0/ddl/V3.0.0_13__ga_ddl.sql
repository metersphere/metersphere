-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS platform_source(
    `platform` VARCHAR(50) NOT NULL   COMMENT '平台名称（国际飞书:LARK_SUITE，飞书:LARK，钉钉:DING_TALK，企业微信:WE_COM）' ,
    `config` BLOB NOT NULL   COMMENT '平台信息配置' ,
    `enable` BIT NOT NULL  DEFAULT 1 COMMENT '是否开启' ,
    `valid` BIT NOT NULL  DEFAULT 0 COMMENT '名称' ,
    PRIMARY KEY (platform)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '平台对接保存参数';

CREATE TABLE IF NOT EXISTS mind_additional_node(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '名称' ,
    `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父节点ID' ,
    `pos` BIGINT NOT NULL  DEFAULT 0 COMMENT '同一节点下的顺序' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '更新人' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '平台对接保存参数';


CREATE INDEX idx_project_id ON mind_additional_node(project_id);
CREATE INDEX idx_name ON mind_additional_node(name);
CREATE INDEX idx_pos ON mind_additional_node(pos);
CREATE INDEX idx_parent_id ON mind_additional_node(parent_id);
CREATE INDEX idx_create_user ON mind_additional_node(create_user);
CREATE INDEX idx_update_user ON mind_additional_node(update_user);
CREATE INDEX idx_create_time ON mind_additional_node(create_time);
CREATE INDEX idx_update_time ON mind_additional_node(update_time);
CREATE UNIQUE INDEX uq_name_project_parent ON mind_additional_node(project_id,name,parent_id);

-- 测试计划增加排序字段
alter table test_plan
    ADD COLUMN `pos` BIGINT NOT NULL DEFAULT 0 COMMENT '自定义排序';

-- 修改计划配置表
ALTER TABLE test_plan_config DROP `test_planning`;

-- 修改计划报告详情表字段
UPDATE test_plan_report_summary SET bug_count = 0 WHERE bug_count IS NULL;
ALTER TABLE test_plan_report_summary MODIFY `bug_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '缺陷数量';
ALTER TABLE test_plan_report_summary DROP `report_count`;

-- 修改计划报告功能用例表字段
UPDATE test_plan_report_function_case SET function_case_bug_count = 0 WHERE function_case_bug_count IS NULL;
ALTER TABLE test_plan_report_function_case MODIFY `function_case_bug_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '功能用例关联缺陷数';

-- 修改计划报告缺陷表字段
UPDATE test_plan_report_bug SET bug_case_count = 0 WHERE bug_case_count IS NULL;
ALTER TABLE test_plan_report_bug MODIFY `bug_case_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '缺陷用例数';

-- 修改测试计划关联接口表字段
ALTER TABLE test_plan_api_case DROP COLUMN num;
ALTER TABLE test_plan_api_case ADD COLUMN test_plan_collection_id VARCHAR(50) NOT NULL COMMENT '测试计划集id';
ALTER TABLE test_plan_api_case ADD COLUMN last_exec_time BIGINT COMMENT '最后执行时间';
CREATE INDEX idx_test_plan_collection_id ON test_plan_api_case(test_plan_collection_id);
CREATE INDEX idx_pos ON test_plan_api_case(pos);

-- 修改测试计划关联场景表字段
ALTER TABLE test_plan_api_scenario DROP COLUMN num;
ALTER TABLE test_plan_api_scenario ADD COLUMN test_plan_collection_id VARCHAR(50) NOT NULL COMMENT '测试计划集id';
ALTER TABLE test_plan_api_scenario ADD COLUMN grouped BIT DEFAULT 0 COMMENT '是否为环境组';
ALTER TABLE test_plan_api_scenario MODIFY COLUMN `environment_id` VARCHAR(50) COMMENT '所属环境或环境组id';
ALTER TABLE test_plan_api_scenario ADD COLUMN last_exec_time BIGINT COMMENT '最后执行时间';
CREATE INDEX idx_test_plan_collection_id ON test_plan_api_scenario(test_plan_collection_id);
CREATE INDEX idx_pos ON test_plan_api_scenario(pos);

-- 修改测试计划关联功能用例表字段
ALTER TABLE test_plan_functional_case ADD COLUMN test_plan_collection_id VARCHAR(50) NOT NULL COMMENT '测试计划集id';
CREATE INDEX idx_test_plan_collection_id ON test_plan_functional_case(test_plan_collection_id);

-- 修改测试规划配置表
ALTER TABLE test_plan_allocation DROP `run_mode_config`;
ALTER TABLE test_plan_allocation ADD `test_resource_pool_id` VARCHAR(50) NOT NULL COMMENT '资源池ID';
ALTER TABLE test_plan_allocation ADD `retry_on_fail` BIT NOT NULL  DEFAULT 0 COMMENT '是否失败重试' ;
ALTER TABLE test_plan_allocation ADD `retry_type` VARCHAR(50) NOT NULL   COMMENT '失败重试类型(步骤/场景)' ;
ALTER TABLE test_plan_allocation ADD `retry_times` INT NOT NULL  DEFAULT 1 COMMENT '失败重试次数' ;
ALTER TABLE test_plan_allocation ADD `retry_interval` INT NOT NULL  DEFAULT 1000 COMMENT '失败重试间隔(单位: ms)' ;
ALTER TABLE test_plan_allocation ADD `stop_on_fail` BIT NOT NULL  DEFAULT 0 COMMENT '是否失败停止' ;
CREATE INDEX idx_resource_pool_id ON test_plan_allocation(test_resource_pool_id);

-- 测试规划集类型
CREATE TABLE IF NOT EXISTS test_plan_allocation_type(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '测试集类型名称' ,
    `type` VARCHAR(50) NOT NULL   COMMENT '测试集类型(功能/接口/场景)' ,
    `execute_method` VARCHAR(50) NOT NULL   COMMENT '执行方式(串行/并行)' ,
    `pos` BIGINT NOT NULL   COMMENT '自定义排序，间隔为2的N次幂' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试集类型';
CREATE INDEX idx_test_plan_id ON test_plan_allocation_type(test_plan_id);

-- 测试集
CREATE TABLE IF NOT EXISTS test_plan_collection(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID' ,
    `test_collection_type_id` VARCHAR(50) NOT NULL   COMMENT '所属测试集类型ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '测试集名称' ,
    `execute_method` VARCHAR(50) NOT NULL   COMMENT '执行方式(串行/并行)' ,
    `grouped` BIT NOT NULL  DEFAULT 0 COMMENT '是否使用环境组' ,
    `environment_id` VARCHAR(50) NOT NULL   COMMENT '环境ID/环境组ID' ,
    `pos` BIGINT NOT NULL   COMMENT '自定义排序，间隔为2的N次幂' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试集';
CREATE INDEX idx_test_plan_id ON test_plan_collection(test_plan_id);
CREATE INDEX idx_collection_type_id ON test_plan_collection(test_collection_type_id);
CREATE INDEX idx_env_id ON test_plan_collection(environment_id);
CREATE INDEX idx_create_user ON test_plan_collection(create_user);


ALTER TABLE api_report ADD COLUMN exec_status VARCHAR(20) NOT NULL  DEFAULT 'PENDING' COMMENT '执行状态';
ALTER TABLE api_report ALTER COLUMN status set DEFAULT '-';
CREATE INDEX idx_exec_status ON api_report(exec_status);

ALTER TABLE api_scenario_report ADD COLUMN exec_status VARCHAR(20) NOT NULL  DEFAULT 'PENDING' COMMENT '执行状态';
ALTER TABLE api_scenario_report ALTER COLUMN status set DEFAULT '-';
CREATE INDEX idx_exec_status ON api_scenario_report(exec_status);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;



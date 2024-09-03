-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 修改测试计划接口报告详情&&场景报告详情表 (缺陷统计字段)
ALTER TABLE test_plan_report_api_case ADD COLUMN api_case_bug_count bigint default 0 not null comment '接口用例关联缺陷数';
ALTER TABLE test_plan_report_api_scenario ADD COLUMN api_scenario_bug_count bigint default 0 not null comment '场景用例关联缺陷数';

-- 创建用户视图表
CREATE TABLE IF NOT EXISTS user_view(
    `id` VARCHAR(50) NOT NULL   COMMENT '视图ID' ,
    `user_id` VARCHAR(50) NOT NULL   COMMENT '用户ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '视图名称' ,
    `view_type` VARCHAR(50) NOT NULL   COMMENT '视图类型，例如功能用例视图' ,
    `scope_id` VARCHAR(50) NOT NULL   COMMENT '视图的应用范围，一般为项目ID' ,
    `pos` BIGINT NOT NULL   COMMENT '自定义排序' ,
    `search_mode` VARCHAR(10) NOT NULL  DEFAULT 'AND' COMMENT '匹配模式：AND/OR' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '用户视图';
-- 创建用户视图索引
CREATE INDEX idx_user_id_scope_id_type ON user_view(`user_id`,`view_type`,`scope_id`);

-- 创建用户视图条件项表
CREATE TABLE IF NOT EXISTS user_view_condition(
    `id` VARCHAR(50) NOT NULL   COMMENT '条件ID' ,
    `user_view_id` VARCHAR(50) NOT NULL   COMMENT '视图ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '参数名称' ,
    `value` VARCHAR(1000)    COMMENT '查询的期望值' ,
    `value_type` VARCHAR(20)   DEFAULT 'STRING' COMMENT '期望值的数据类型：STRING,INT,FLOAT,ARRAY' ,
    `custom_field` BIT NOT NULL  DEFAULT 1 COMMENT '是否为自定义字段' ,
    `operator` VARCHAR(50)    COMMENT '操作符：等于、大于、小于、等' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户视图条件项';
-- 创建用户视图条件项索引
CREATE INDEX idx_user_view_id ON user_view_condition(`user_view_id`);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
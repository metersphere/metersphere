-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE user_key MODIFY COLUMN description VARCHAR(1000);

ALTER TABLE api_definition_mock ADD COLUMN status_code INT(50) ;

CREATE INDEX idx_scene ON custom_field (scene);
CREATE INDEX idx_internal ON custom_field (internal);

CREATE INDEX idx_num ON test_plan(num);
ALTER TABLE test_plan_config DROP COLUMN run_mode_config;

ALTER TABLE test_plan_config ADD COLUMN test_planning BIT NOT NULL  DEFAULT 0 COMMENT '是否开启测试规划';

ALTER TABLE api_definition_mock ADD COLUMN update_user VARCHAR(50) COMMENT '更新人';
ALTER TABLE api_definition_mock ADD COLUMN version_id VARCHAR(50) COMMENT '版本id';

ALTER TABLE operation_history MODIFY COLUMN module VARCHAR(100);

ALTER TABLE operation_log MODIFY COLUMN module VARCHAR(100);

CREATE INDEX idx_num ON test_plan_functional_case(num);

ALTER TABLE test_resource_pool DROP COLUMN api_test;
ALTER TABLE test_resource_pool DROP COLUMN load_test;
ALTER TABLE test_resource_pool DROP COLUMN ui_test;
DROP TABLE test_plan_bug;


CREATE TABLE IF NOT EXISTS test_plan_allocation
(
    `id`           VARCHAR(50) NOT NULL COMMENT 'ID',
    `test_plan_id` VARCHAR(50) NOT NULL COMMENT '测试计划ID',
    `run_mode_config` LONGBLOB NOT NULL COMMENT '运行配置',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '测试计划配置';

CREATE INDEX idx_test_plan_id ON test_plan_allocation(test_plan_id);

ALTER TABLE test_plan_functional_case DROP COLUMN num;

CREATE TABLE IF NOT EXISTS test_plan_case_execute_history(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_case_id` VARCHAR(50) NOT NULL   COMMENT '计划关联用例表ID' ,
    `case_id` VARCHAR(50) NOT NULL   COMMENT '用例ID' ,
    `status` VARCHAR(64) NOT NULL   COMMENT '执行结果：成功/失败/阻塞' ,
    `content` LONGBLOB    COMMENT '执行评论意见' ,
    `steps` LONGBLOB    COMMENT '用例步骤执行记录（JSON)，step_model 为 Step 时启用' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是取消关联或执行被删除的：0-否，1-是' ,
    `notifier` VARCHAR(1000)    COMMENT '通知人' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '操作人' ,
    `create_time` BIGINT NOT NULL   COMMENT '操作时间' ,
    PRIMARY KEY (id)
    )ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例执行历史表';


CREATE INDEX idx_test_plan_case_id ON test_plan_case_execute_history(test_plan_case_id);
CREATE INDEX idx_status ON test_plan_case_execute_history(status);
CREATE INDEX idx_deleted ON test_plan_case_execute_history(deleted);

-- 计划报告
CREATE TABLE IF NOT EXISTS test_plan_report(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '报告名称' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `start_time` BIGINT    COMMENT '开始时间' ,
    `end_time` BIGINT    COMMENT '结束时间' ,
    `trigger_mode` VARCHAR(50)    COMMENT '触发类型' ,
    `exec_status` VARCHAR(50) NOT NULL  DEFAULT 'PENDING' COMMENT '执行状态: 未执行, 执行中, 已停止, 已完成;' ,
    `result_status` VARCHAR(50)   DEFAULT '-' COMMENT '结果状态: 成功, 失败, 阻塞, 误报' ,
    `pass_threshold` VARCHAR(100) NOT NULL   COMMENT '通过阈值' ,
    `pass_rate` DECIMAL    COMMENT '通过率' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目id' ,
    `integrated` BIT NOT NULL  DEFAULT 0 COMMENT '是否是集成报告' ,
    `deleted` BIT NOT NULL  DEFAULT 0 COMMENT '是否删除' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告';


CREATE INDEX idx_test_plan_id ON test_plan_report(test_plan_id);
CREATE INDEX idx_create_user ON test_plan_report(create_user);
CREATE INDEX idx_create_time ON test_plan_report(create_time);
CREATE INDEX idx_exec_status ON test_plan_report(exec_status);
CREATE INDEX idx_result_status ON test_plan_report(result_status);
CREATE INDEX idx_pass_rate ON test_plan_report(pass_rate);
CREATE INDEX idx_project_id ON test_plan_report(project_id);
CREATE INDEX idx_integrated ON test_plan_report(integrated);
CREATE INDEX idx_deleted ON test_plan_report(deleted);

CREATE TABLE IF NOT EXISTS test_plan_report_summary(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `functional_case_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '功能用例数量' ,
    `api_case_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '接口用例数量' ,
    `api_scenario_count` BIGINT NOT NULL  DEFAULT 0 COMMENT '场景用例数量' ,
    `bug_count` BIGINT(255) NOT NULL   COMMENT '缺陷数量' ,
    `test_plan_report_id` VARCHAR(50) NOT NULL   COMMENT '测试计划报告ID' ,
    `summary` VARCHAR(1000)    COMMENT '总结' ,
    `report_count` BLOB    COMMENT '报告统计内容' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告内容统计';

CREATE UNIQUE INDEX idx_test_plan_report_id ON test_plan_report_summary(test_plan_report_id);

CREATE TABLE IF NOT EXISTS test_plan_report_function_case(
     `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
     `test_plan_report_id` VARCHAR(50) NOT NULL   COMMENT '测试计划报告ID' ,
     `test_plan_function_case_id` VARCHAR(50) NOT NULL   COMMENT '测试计划功能用例关联ID' ,
     `function_case_id` VARCHAR(50) NOT NULL   COMMENT '功能用例ID' ,
     `execute_result` VARCHAR(50) NOT NULL   COMMENT '执行结果' ,
     PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告内容功能用例部分';

CREATE UNIQUE INDEX idx_test_plan_report_id ON test_plan_report_function_case(test_plan_report_id);

CREATE TABLE IF NOT EXISTS test_plan_report_bug(
    `id` VARCHAR(50)    COMMENT 'ID' ,
    `test_plan_report_id` VARCHAR(50)    COMMENT '报告ID' ,
    `bug_id` VARCHAR(50)    COMMENT '缺陷ID'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告内容缺陷部分';

CREATE UNIQUE INDEX idx_test_plan_report_id ON test_plan_report_bug(test_plan_report_id);

-- 场景步骤 csv 表增加场景ID字段
ALTER TABLE api_scenario_csv_step ADD scenario_id varchar(50) NOT NULL COMMENT '场景ID';
CREATE INDEX idx_scenario_id USING BTREE ON api_scenario_csv_step (scenario_id);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;



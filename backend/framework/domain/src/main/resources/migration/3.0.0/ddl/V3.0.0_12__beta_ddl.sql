-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE user_key MODIFY COLUMN description VARCHAR(1000);

-- add mock table column
ALTER TABLE api_definition_mock ADD COLUMN status_code INT(50) ;
ALTER TABLE api_definition_mock ADD COLUMN update_user VARCHAR(50) COMMENT '更新人';
ALTER TABLE api_definition_mock ADD COLUMN version_id VARCHAR(50) COMMENT '版本id';

CREATE INDEX idx_scene ON custom_field (scene);
CREATE INDEX idx_internal ON custom_field (internal);

-- edit test_plan_config column
ALTER TABLE test_plan_config DROP COLUMN run_mode_config;
ALTER TABLE test_plan_config ADD COLUMN test_planning BIT NOT NULL  DEFAULT 0 COMMENT '是否开启测试规划';
ALTER TABLE test_plan_config MODIFY pass_threshold DECIMAL(10, 2) NOT NULL;


ALTER TABLE operation_history MODIFY COLUMN module VARCHAR(100);
CREATE INDEX idx_source_id ON operation_history(`source_id`);

ALTER TABLE operation_log MODIFY COLUMN module VARCHAR(100);

-- drop test_resource_pool column
ALTER TABLE test_plan_functional_case DROP COLUMN num;
ALTER TABLE test_resource_pool DROP COLUMN api_test;
ALTER TABLE test_resource_pool DROP COLUMN load_test;
ALTER TABLE test_resource_pool DROP COLUMN ui_test;


CREATE TABLE IF NOT EXISTS test_plan_allocation(
    `id`           VARCHAR(50) NOT NULL COMMENT 'ID',
    `test_plan_id` VARCHAR(50) NOT NULL COMMENT '测试计划ID',
    `run_mode_config` LONGBLOB NOT NULL COMMENT '运行配置',
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '测试计划配置';

CREATE INDEX idx_test_plan_id ON test_plan_allocation(test_plan_id);


CREATE TABLE IF NOT EXISTS test_plan_case_execute_history(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_case_id` VARCHAR(50) NOT NULL   COMMENT '计划关联用例表ID' ,
    `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划id' ,
    `case_id` VARCHAR(50) NOT NULL   COMMENT '用例ID' ,
    `status` VARCHAR(64) NOT NULL   COMMENT '执行结果：成功/失败/阻塞' ,
    `content` LONGBLOB    COMMENT '执行评论意见' ,
    `steps` LONGBLOB    COMMENT '用例步骤执行记录（JSON)，step_model 为 Step 时启用' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是取消关联或执行被删除的：0-否，1-是' ,
    `notifier` VARCHAR(1000)    COMMENT '通知人' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '操作人' ,
    `create_time` BIGINT NOT NULL   COMMENT '操作时间' ,
    PRIMARY KEY (id)
)   ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例执行历史表';


CREATE INDEX idx_test_plan_case_id ON test_plan_case_execute_history(test_plan_case_id);
CREATE INDEX idx_status ON test_plan_case_execute_history(status);
CREATE INDEX idx_deleted ON test_plan_case_execute_history(deleted);
CREATE INDEX idx_test_plan_id ON test_plan_case_execute_history(test_plan_id);
CREATE INDEX idx_case_id ON test_plan_case_execute_history(case_id);

-- 计划报告
CREATE TABLE IF NOT EXISTS test_plan_report(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID' ,
    `name` VARCHAR(300) NOT NULL   COMMENT '报告名称' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `execute_time` BIGINT    COMMENT '执行时间;计划真正执行的时间' ,
    `start_time` BIGINT    COMMENT '开始时间;计划开始执行的时间' ,
    `end_time` BIGINT    COMMENT '结束时间;计划结束执行的时间' ,
    `exec_status` VARCHAR(50) NOT NULL  DEFAULT 'PENDING' COMMENT '执行状态' ,
    `result_status` VARCHAR(50) NOT NULL  DEFAULT '-' COMMENT '结果状态' ,
    `pass_rate` DECIMAL(10, 4)    COMMENT '通过率' ,
    `trigger_mode` VARCHAR(50) NOT NULL   COMMENT '触发类型' ,
    `pass_threshold` DECIMAL(10, 2) NOT NULL   COMMENT '通过阈值' ,
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
    `summary` LONGTEXT    COMMENT '总结' ,
    `report_count` BLOB    COMMENT '报告统计内容' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告内容统计';

-- 为测试计划报告内容统计表添加唯一索引
CREATE UNIQUE INDEX un_idx_test_plan_report_id ON test_plan_report_summary(test_plan_report_id);

CREATE TABLE IF NOT EXISTS test_plan_report_function_case(
     `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
     `test_plan_report_id` VARCHAR(50) NOT NULL   COMMENT '测试计划报告ID' ,
     `test_plan_function_case_id` VARCHAR(50) NOT NULL   COMMENT '测试计划功能用例关联ID(同一计划下可重复关联, 暂时保留)' ,
     `function_case_id` VARCHAR(50) NOT NULL   COMMENT '功能用例ID' ,
     `function_case_num` BIGINT NOT NULL   COMMENT '功能用例业务ID' ,
     `function_case_name` VARCHAR(255)  NOT NULL  COMMENT '功能用例名称' ,
     `function_case_module` VARCHAR(255)    COMMENT '功能用例所属模块' ,
     `function_case_priority` VARCHAR(50)    COMMENT '功能用例用例等级' ,
     `function_case_execute_user` VARCHAR(50)    COMMENT '功能用例执行人' ,
     `function_case_bug_count` BIGINT    COMMENT '功能用例关联缺陷数' ,
     `function_case_execute_result` VARCHAR(50) NOT NULL   COMMENT '执行结果' ,
     PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告内容功能用例部分';

CREATE INDEX idx_test_plan_report_id ON test_plan_report_function_case(test_plan_report_id);

CREATE TABLE IF NOT EXISTS test_plan_report_bug(
   `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
   `test_plan_report_id` VARCHAR(50) NOT NULL   COMMENT '测试计划报告ID' ,
   `bug_id` VARCHAR(50) NOT NULL   COMMENT '缺陷ID' ,
   `bug_num` BIGINT NOT NULL   COMMENT '缺陷业务ID' ,
   `bug_title` VARCHAR(255) NOT NULL   COMMENT '缺陷标题' ,
   `bug_status` VARCHAR(50)    COMMENT '缺陷状态' ,
   `bug_handle_user` VARCHAR(50)    COMMENT '缺陷处理人' ,
   `bug_case_count` BIGINT NOT NULL   COMMENT '缺陷用例数' ,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告内容缺陷部分';

CREATE INDEX idx_test_plan_report_id ON test_plan_report_bug(test_plan_report_id);

CREATE TABLE IF NOT EXISTS test_plan_report_attachment(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_report_id` VARCHAR(50) NOT NULL   COMMENT '报告ID' ,
    `file_id` VARCHAR(50) NOT NULL   COMMENT '文件ID' ,
    `file_name` VARCHAR(255) NOT NULL   COMMENT '文件名称' ,
    `size` BIGINT NOT NULL   COMMENT '文件大小' ,
    `source` VARCHAR(255) NOT NULL   COMMENT '文件来源' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告附件关系表';

CREATE INDEX idx_report_id ON test_plan_report_attachment(test_plan_report_id);
CREATE INDEX idx_file_id ON test_plan_report_attachment(file_id);
CREATE INDEX idx_source ON test_plan_report_attachment(source);

-- 场景步骤 csv 表增加场景ID字段
ALTER TABLE api_scenario_csv_step ADD scenario_id varchar(50) NOT NULL COMMENT '场景ID';
CREATE INDEX idx_scenario_id USING BTREE ON api_scenario_csv_step (scenario_id);
CREATE INDEX idx_report_id ON api_scenario_report_step(report_id);


-- 修改测试计划模块名称长度
ALTER TABLE test_plan_module  MODIFY COLUMN `name` varchar(255);

CREATE INDEX idx_num ON test_plan(num);
ALTER TABLE test_plan DROP INDEX uq_name_project;


-- 修改缺陷自定义字段值长度(由于要支持三方平台富文本存储)
ALTER TABLE bug_custom_field MODIFY `value` longtext;

-- 场景报告步骤结果内容
CREATE TABLE IF NOT EXISTS api_scenario_report_detail_blob(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `report_id` VARCHAR(50) NOT NULL   COMMENT '报告fk' ,
    `content` LONGBLOB    COMMENT '执行结果' ,
    PRIMARY KEY (id)
)   ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '场景报告步骤结果内容';

ALTER TABLE `api_scenario_report_detail_blob`
    ADD INDEX `idx_report_id`(`report_id`) USING BTREE;

-- 兼容处理历史数据
INSERT INTO api_scenario_report_detail_blob (id, report_id, content)
SELECT id, report_id, content FROM api_scenario_report_detail;

-- 删除原有的内容字段
ALTER TABLE api_scenario_report_detail DROP COLUMN content;

-- 修改 expression 为 text 类型
ALTER TABLE fake_error DROP INDEX project_id_status;
ALTER TABLE fake_error MODIFY COLUMN expression text NOT NULL COMMENT '期望值';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;




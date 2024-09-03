-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 修改测试计划接口报告详情&&场景报告详情表 (缺陷统计字段)
ALTER TABLE test_plan_report_api_case ADD COLUMN api_case_bug_count bigint default 0 not null comment '接口用例关联缺陷数';
ALTER TABLE test_plan_report_api_scenario ADD COLUMN api_scenario_bug_count bigint default 0 not null comment '场景用例关联缺陷数';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
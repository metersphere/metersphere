SET SESSION innodb_lock_wait_timeout = 7200;

-- 测试计划相关的索引
CREATE INDEX api_case_id_IDX ON test_plan_api_case (api_case_id);
CREATE INDEX test_id_IDX ON test_case_test (test_id);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
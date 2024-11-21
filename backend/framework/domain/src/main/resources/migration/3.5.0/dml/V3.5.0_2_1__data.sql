-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 批量更新执行状态为空串
UPDATE test_plan_functional_case SET last_exec_result = 'PENDING' WHERE last_exec_result is null OR last_exec_result = '' OR last_exec_result = '-';
UPDATE test_plan_api_case SET last_exec_result = '' WHERE last_exec_result is null OR last_exec_result = 'PENDING' OR last_exec_result = '-';
UPDATE test_plan_api_scenario SET last_exec_result = '' WHERE last_exec_result is null OR last_exec_result = 'PENDING' OR last_exec_result = '-';
UPDATE api_test_case SET last_report_status = '' WHERE last_report_status = 'PENDING' OR last_report_status = '-';
UPDATE api_scenario SET last_report_status = '' WHERE last_report_status = 'PENDING' OR last_report_status = '-';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
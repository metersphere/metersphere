-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 处理用例历史数据
update functional_case set last_execute_result = 'PENDING' where last_execute_result = 'UN_EXECUTED';
update functional_case set last_execute_result = 'SUCCESS' where last_execute_result = 'PASSED';
update functional_case set last_execute_result = 'ERROR' where last_execute_result = 'FAILED';
update functional_case set last_execute_result = 'PENDING' where last_execute_result = 'SKIPPED';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

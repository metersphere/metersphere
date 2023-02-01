SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE test_plan_report ADD components VARCHAR (20);
SET SESSION innodb_lock_wait_timeout = DEFAULT;

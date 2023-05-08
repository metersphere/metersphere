SET SESSION innodb_lock_wait_timeout = 7200;


-- 队列增加执行人
ALTER TABLE test_plan_execution_queue
    ADD COLUMN `execute_user` VARCHAR(50);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE message_task
    ADD test_id varchar(50) NULL;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

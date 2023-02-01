SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE message_task
    ADD create_time bigint(13) DEFAULT 0;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE schedule
    MODIFY COLUMN id VARCHAR (255);
ALTER TABLE message_task
    MODIFY COLUMN id VARCHAR (255);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

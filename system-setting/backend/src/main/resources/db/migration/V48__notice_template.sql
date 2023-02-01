SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE message_task
    MODIFY identification varchar(50) NOT NULL;

ALTER TABLE message_task
    MODIFY organization_id varchar(50) NULL;

ALTER TABLE message_task
    MODIFY test_id varchar(50) NULL;

ALTER TABLE message_task
    ADD template TEXT NULL;

DROP TABLE IF EXISTS notice;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

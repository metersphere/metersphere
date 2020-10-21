ALTER TABLE message_task
    ADD identification varchar(255) NOT NULL;
ALTER TABLE message_task
    ADD result tinyint(1) NOT NULL;
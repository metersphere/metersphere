SET SESSION innodb_lock_wait_timeout = 7200;
alter table message_task
    add organization_id varchar(255) null;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

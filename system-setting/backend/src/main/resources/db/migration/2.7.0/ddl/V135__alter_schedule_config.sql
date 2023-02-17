SET SESSION innodb_lock_wait_timeout = 7200;

alter table schedule
    modify column config varchar(1000);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
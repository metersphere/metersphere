SET SESSION innodb_lock_wait_timeout = 7200;
alter table test_plan
    add creator varchar(255) not null;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

SET SESSION innodb_lock_wait_timeout = 7200;
alter table test_case modify prerequisite varchar(500) null comment 'Test case prerequisite condition';
SET SESSION innodb_lock_wait_timeout = DEFAULT;

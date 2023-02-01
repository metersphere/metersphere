SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE test_case MODIFY COLUMN name varchar(255) NOT NULL COMMENT 'Test case name';
SET SESSION innodb_lock_wait_timeout = DEFAULT;

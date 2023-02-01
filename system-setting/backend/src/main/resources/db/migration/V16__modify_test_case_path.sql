SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE test_case MODIFY COLUMN node_path varchar(999) NOT NULL COMMENT 'Node path this case belongs to' ;

SET SESSION innodb_lock_wait_timeout = DEFAULT;

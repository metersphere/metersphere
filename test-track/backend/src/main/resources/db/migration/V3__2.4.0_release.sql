SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE test_case_issues ADD relate_time BIGINT(13) NULL COMMENT 'relate time';

SET SESSION innodb_lock_wait_timeout = DEFAULT;
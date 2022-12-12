SET SESSION innodb_lock_wait_timeout = 7200;

-- 用例评审添加通过标准
ALTER TABLE test_case_review ADD review_pass_rule varchar(20) default 'SINGLE';

SET SESSION innodb_lock_wait_timeout = DEFAULT;
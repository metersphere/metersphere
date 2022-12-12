SET SESSION innodb_lock_wait_timeout = 7200;

-- 评论添加所属的评审ID，或者测试计划ID
ALTER TABLE test_case_comment ADD belong_id varchar(50) NULL;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
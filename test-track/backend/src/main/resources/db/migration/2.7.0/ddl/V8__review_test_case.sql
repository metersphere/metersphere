SET SESSION innodb_lock_wait_timeout = 7200;

-- 评论的创建时间添加索引
CREATE INDEX test_case_comment_create_time_IDX ON test_case_comment (create_time);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
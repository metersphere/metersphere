SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化数据
INSERT INTO test_case_review_test_case_users
SELECT DISTINCT t1.case_id, t2.review_id, t2.user_id
FROM test_case_review_test_case t1,
     test_case_review_users t2
WHERE t1.review_id = t2.review_id;

SET SESSION innodb_lock_wait_timeout = DEFAULT;

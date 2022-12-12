SET SESSION innodb_lock_wait_timeout = 7200;

INSERT INTO test_case_comment (id, case_id,  description, author, create_time, update_time, status, type, belong_id)
SELECT UUID() AS id, tcc.case_id, tcc.description, tcc.author, tcc.create_time, tcc.update_time, tcc.status, tcc.type, tcrtc.review_id as belong_id
FROM  test_case_comment tcc
          JOIN test_case_review_test_case tcrtc on tcrtc.case_id = tcc.case_id
WHERE tcc.type = 'REVIEW';

-- 删除 belong_id 为 NULL 的历史数据
DELETE FROM test_case_comment WHERE  type = 'REVIEW' AND belong_id IS NULL;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
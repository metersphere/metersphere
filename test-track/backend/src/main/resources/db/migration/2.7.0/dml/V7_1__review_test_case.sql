SET SESSION innodb_lock_wait_timeout = 7200;

-- 给历史的评审评论，设置评审ID
INSERT INTO test_case_comment (id, case_id,  description, author, create_time, update_time, status, type, belong_id)
SELECT UUID() AS id, tcc.case_id, tcc.description, tcc.author, tcc.create_time, tcc.update_time, tcc.status, tcc.type, tcrtc.review_id as belong_id
FROM  test_case_comment tcc
          JOIN test_case_review_test_case tcrtc on tcrtc.case_id = tcc.case_id
WHERE tcc.type = 'REVIEW';

-- 删除 belong_id 为 NULL 的历史数据
DELETE FROM test_case_comment WHERE  type = 'REVIEW' AND belong_id IS NULL;

-- 历史数据 Prepare 的评审评论状态改为 null
UPDATE test_case_comment SET status = NULL WHERE status = 'Prepare' AND `type` = 'REVIEW';

-- 将评审用例历史数据的未评审改成评审中
UPDATE test_case_review_test_case SET status = 'Underway' WHERE status = 'Prepare';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

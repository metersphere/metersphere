SET SESSION innodb_lock_wait_timeout = 7200;

-- 拆分test_case表联合索引并补充新索引
DROP INDEX test_case_project_id_order_IDX ON test_case;
CREATE INDEX test_case_project_status_latest ON test_case(project_id, status, latest);
CREATE INDEX test_case_order ON test_case(`order`);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
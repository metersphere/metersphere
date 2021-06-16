-- 是否自定更新功能用例状态
ALTER TABLE test_plan ADD automatic_status_update TINYINT(1) DEFAULT 0 NULL COMMENT '是否自定更新功能用例状态';

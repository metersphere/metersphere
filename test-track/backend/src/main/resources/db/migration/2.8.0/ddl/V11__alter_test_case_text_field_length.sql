SET SESSION innodb_lock_wait_timeout = 7200;

-- 文本字段改成 LONGTEXT
ALTER TABLE test_case MODIFY COLUMN prerequisite LONGTEXT NULL COMMENT 'Test case prerequisite condition';
ALTER TABLE test_case MODIFY COLUMN remark LONGTEXT NULL COMMENT 'Test case remark';
ALTER TABLE test_case MODIFY COLUMN steps LONGTEXT NULL COMMENT 'Test case steps (JSON format)';
ALTER TABLE test_case MODIFY COLUMN step_description LONGTEXT NULL;
ALTER TABLE test_case MODIFY COLUMN expected_result LONGTEXT NULL;

ALTER TABLE custom_field_issues MODIFY COLUMN text_value LONGTEXT NULL;
ALTER TABLE custom_field_test_case MODIFY COLUMN text_value LONGTEXT NULL;

SET SESSION innodb_lock_wait_timeout = DEFAULT;

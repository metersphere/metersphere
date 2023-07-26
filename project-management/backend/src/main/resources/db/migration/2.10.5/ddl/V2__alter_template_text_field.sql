SET SESSION innodb_lock_wait_timeout = 7200;

-- 将 TEXT 改成 LONGTEXT
ALTER TABLE test_case_template MODIFY COLUMN prerequisite LONGTEXT NULL COMMENT 'Test case prerequisite';
ALTER TABLE test_case_template MODIFY COLUMN step_description LONGTEXT NULL COMMENT 'Test case steps desc';
ALTER TABLE test_case_template MODIFY COLUMN expected_result LONGTEXT NULL COMMENT 'Test case expected result';
ALTER TABLE test_case_template MODIFY COLUMN actual_result LONGTEXT NULL COMMENT 'Test case actual result';
ALTER TABLE test_case_template MODIFY COLUMN steps LONGTEXT NULL COMMENT 'Test case step';
ALTER TABLE custom_field_template MODIFY COLUMN default_value LONGTEXT NULL;
ALTER TABLE issue_template MODIFY COLUMN content LONGTEXT NULL COMMENT 'Issue content';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

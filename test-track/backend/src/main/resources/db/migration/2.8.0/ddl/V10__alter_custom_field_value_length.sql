SET SESSION innodb_lock_wait_timeout = 7200;

-- 增加自定义字段值的长度
ALTER TABLE custom_field_issues MODIFY COLUMN `value` varchar(1000) NULL;
ALTER TABLE custom_field_test_case MODIFY COLUMN `value` varchar(1000) NULL;

SET SESSION innodb_lock_wait_timeout = DEFAULT;

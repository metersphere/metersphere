SET SESSION innodb_lock_wait_timeout = 7200;
-- 自定义字段保存表头的key
ALTER TABLE custom_field_template ADD `key` varchar(1) NULL COMMENT 'Save Table Header Key';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

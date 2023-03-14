SET SESSION innodb_lock_wait_timeout = 7200;

-- 修改自定义字段用例等级为必填字段
UPDATE custom_field_template SET required = 1 WHERE field_id IN (
    SELECT id FROM custom_field WHERE scene = 'TEST_CASE' AND name = '用例等级' AND `system` is true
);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

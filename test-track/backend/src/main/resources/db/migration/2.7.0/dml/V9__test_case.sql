SET SESSION innodb_lock_wait_timeout = 7200;

-- 处理历史数据中自定义字段为空的数据
UPDATE test_case SET custom_num = num WHERE custom_num IS NULL OR custom_num = '';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

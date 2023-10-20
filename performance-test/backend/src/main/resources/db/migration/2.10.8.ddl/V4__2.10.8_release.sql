SET SESSION innodb_lock_wait_timeout = 7200;
--
-- 增加一个索引
ALTER TABLE load_test_report_result_realtime
    MODIFY COLUMN `report_value` longtext NULL;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
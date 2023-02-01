SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE load_test_report
    ADD file_id VARCHAR(50) NULL;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

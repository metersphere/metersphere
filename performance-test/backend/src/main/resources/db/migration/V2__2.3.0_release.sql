SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE load_test_report
    MODIFY name VARCHAR(255) NOT NULL;

ALTER TABLE load_test_report
    MODIFY test_name VARCHAR(255) NULL;


SET SESSION innodb_lock_wait_timeout = DEFAULT;
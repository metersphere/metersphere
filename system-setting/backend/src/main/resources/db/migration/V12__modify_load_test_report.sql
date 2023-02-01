SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE load_test_report
    ADD load_configuration LONGTEXT NULL;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

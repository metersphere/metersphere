SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE test_case
    ADD  other_test_name varchar(200);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

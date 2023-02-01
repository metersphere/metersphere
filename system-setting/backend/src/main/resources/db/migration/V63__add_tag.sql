SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE api_definition
    ADD tags VARCHAR(1000) NULL;

ALTER TABLE api_test_case
    ADD tags VARCHAR(1000) NULL;

ALTER TABLE test_case
    ADD tags VARCHAR(1000) NULL;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

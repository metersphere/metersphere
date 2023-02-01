SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE test_resource_pool
    ADD image VARCHAR(100) NULL;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

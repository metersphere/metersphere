SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE schedule
    MODIFY COLUMN `key` VARCHAR(255);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

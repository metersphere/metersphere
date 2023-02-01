SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE notice
    ADD COLUMN id VARCHAR(50);
UPDATE notice
SET id = uuid();
ALTER TABLE notice
    MODIFY COLUMN id VARCHAR(50) PRIMARY KEY;
ALTER TABLE notice
    DROP COLUMN EMAIL;
ALTER TABLE notice
    ADD COLUMN type VARCHAR(100) DEFAULT 'EMAIL';
SET SESSION innodb_lock_wait_timeout = DEFAULT;

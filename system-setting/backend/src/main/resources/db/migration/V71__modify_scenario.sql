SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE api_scenario MODIFY COLUMN id VARCHAR (120);
SET SESSION innodb_lock_wait_timeout = DEFAULT;

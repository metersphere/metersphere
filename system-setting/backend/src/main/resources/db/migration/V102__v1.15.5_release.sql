SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE api_definition_exec_result
    MODIFY COLUMN name VARCHAR (255);
SET SESSION innodb_lock_wait_timeout = DEFAULT;

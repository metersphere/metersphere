SET SESSION innodb_lock_wait_timeout = 7200;
-- file name length change
ALTER TABLE file_metadata
    MODIFY name VARCHAR(250) NOT NULL COMMENT 'File name';
-- api_scenario_report modify column length
ALTER TABLE api_scenario_report MODIFY COLUMN name VARCHAR(300);
SET SESSION innodb_lock_wait_timeout = DEFAULT;

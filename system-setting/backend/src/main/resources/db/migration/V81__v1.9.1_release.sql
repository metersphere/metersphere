SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE api_scenario_report MODIFY COLUMN scenario_name VARCHAR(3000);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

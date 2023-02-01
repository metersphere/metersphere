SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE api_scenario_report ADD scenario_name varchar(255) NULL;
ALTER TABLE api_scenario_report ADD scenario_id varchar(100) NULL;
ALTER TABLE test_plan DROP COLUMN api_ids;
ALTER TABLE test_plan DROP COLUMN scenario_ids;

SET SESSION innodb_lock_wait_timeout = DEFAULT;

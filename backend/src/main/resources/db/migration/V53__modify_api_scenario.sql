ALTER TABLE api_scenario_report ADD execute_type varchar(200) NULL;
ALTER TABLE api_definition MODIFY COLUMN description LONGTEXT NULL COMMENT 'Test description';
ALTER TABLE api_test_case MODIFY COLUMN description LONGTEXT NULL COMMENT 'Test case description';
ALTER TABLE api_scenario MODIFY COLUMN description LONGTEXT NULL COMMENT 'Api scenario description';
ALTER TABLE api_scenario_report MODIFY COLUMN description LONGTEXT NULL COMMENT 'Api scenario report description';
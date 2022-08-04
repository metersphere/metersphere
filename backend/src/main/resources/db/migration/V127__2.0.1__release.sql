--
-- V127__2-0-1_add_test_plan_ui_fail_cases
ALTER TABLE test_plan_report_content ADD COLUMN `ui_failure_cases` LONGTEXT COMMENT 'ui failure cases (JSON format)';

--
-- V128__2-0-1_update_api_scenario_last_result
UPDATE api_scenario set last_result='' where last_result IS NULL;

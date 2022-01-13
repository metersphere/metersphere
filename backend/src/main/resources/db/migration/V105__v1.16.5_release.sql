ALTER TABLE test_plan_report_content ADD plan_scenario_report_struct longtext NULL;
ALTER TABLE test_plan_report_content ADD plan_api_case_report_struct longtext NULL;
ALTER TABLE test_plan_report_content ADD plan_load_case_report_struct longtext NULL;
ALTER TABLE api_execution_queue ADD failure TINYINT(1);

CREATE INDEX api_scenario_report_result_report_id_IDX ON api_scenario_report_result (report_id);

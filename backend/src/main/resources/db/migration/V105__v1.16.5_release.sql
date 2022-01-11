ALTER TABLE test_plan_report_content ADD scenario_report_id longtext NULL;
ALTER TABLE test_plan_report_content ADD api_case_report_id longtext NULL;
ALTER TABLE test_plan_report_content ADD load_case_report_id longtext NULL;

CREATE INDEX api_scenario_report_result_report_id_IDX ON api_scenario_report_result (report_id);

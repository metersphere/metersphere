ALTER TABLE `test_plan_api_case`
    ADD INDEX planIdIndex (`test_plan_id`);
ALTER TABLE `test_plan_api_scenario`
    ADD INDEX planIdIndex (`test_plan_id`);
ALTER TABLE `test_plan_load_case`
    ADD INDEX planIdIndex (`test_plan_id`);
ALTER TABLE `test_case_issues`
    ADD INDEX issues_id_index (`issues_id`);
ALTER TABLE `test_plan_report`
    ADD INDEX planIdIndex (`test_plan_id`);
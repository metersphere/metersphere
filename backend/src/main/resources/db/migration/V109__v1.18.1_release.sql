ALTER TABLE `api_definition_exec_result` ADD INDEX projectIdIndex ( `project_id` );
ALTER TABLE `api_scenario_report` ADD INDEX projectIdIndex ( `project_id` );
ALTER TABLE `api_scenario_report` ADD INDEX projectIdexectypeIndex ( `project_id`,`execute_type` );
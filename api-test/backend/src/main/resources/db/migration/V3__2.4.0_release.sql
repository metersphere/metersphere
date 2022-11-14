ALTER TABLE api_execution_info ADD project_id VARCHAR ( 50 ) NULL;
ALTER TABLE api_execution_info ADD execute_type VARCHAR ( 50 ) NULL;
ALTER TABLE api_execution_info ADD version VARCHAR ( 255 ) NULL;

ALTER TABLE api_case_execution_info ADD project_id VARCHAR ( 50 ) NULL;
ALTER TABLE api_case_execution_info ADD execute_type VARCHAR ( 50 ) NULL;
ALTER TABLE api_case_execution_info ADD version VARCHAR ( 255 ) NULL;

ALTER TABLE scenario_execution_info ADD project_id VARCHAR ( 50 ) NULL;
ALTER TABLE scenario_execution_info ADD execute_type VARCHAR ( 50 ) NULL;
ALTER TABLE scenario_execution_info ADD version VARCHAR ( 255 ) NULL;

ALTER TABLE api_execution_info ADD INDEX idx_project_id(project_id);
ALTER TABLE api_execution_info ADD INDEX idx_execute_type(execute_type);
ALTER TABLE api_execution_info ADD INDEX idx_version ( version );

ALTER TABLE api_case_execution_info ADD INDEX idx_version ( version );
ALTER TABLE api_case_execution_info ADD INDEX idx_project_id(project_id);
ALTER TABLE api_case_execution_info ADD INDEX idx_execute_type(execute_type);

ALTER TABLE scenario_execution_info ADD INDEX idx_version ( version );
ALTER TABLE scenario_execution_info ADD INDEX idx_project_id(project_id);
ALTER TABLE scenario_execution_info ADD INDEX idx_execute_type(execute_type);
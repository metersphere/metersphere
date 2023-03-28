SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE api_execution_info ADD report_id VARCHAR ( 50 ) NULL;
ALTER TABLE api_case_execution_info ADD report_id VARCHAR ( 50 ) NULL;
ALTER TABLE scenario_execution_info ADD report_id VARCHAR ( 50 ) NULL;
ALTER TABLE api_execution_info ADD INDEX idx_report_id(report_id);
ALTER TABLE api_case_execution_info ADD INDEX idx_report_id(report_id);
ALTER TABLE scenario_execution_info ADD INDEX idx_report_id(report_id);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
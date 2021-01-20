ALTER TABLE api_test_case drop COLUMN response;
ALTER TABLE api_test_case add COLUMN last_result_id varchar(64) COMMENT 'Last ApiDefinitionExecResult ID';
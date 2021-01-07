ALTER TABLE api_definition
    ADD tag VARCHAR(1000) NULL;

ALTER TABLE api_test_case
    ADD tag VARCHAR(1000) NULL;

ALTER TABLE test_case
    ADD tag VARCHAR(1000) NULL;
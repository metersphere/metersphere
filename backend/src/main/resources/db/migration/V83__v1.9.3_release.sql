-- load_test_report
ALTER TABLE load_test_report
    ADD project_id VARCHAR(50) NULL;

ALTER TABLE load_test_report
    ADD test_name VARCHAR(64) NULL;

ALTER TABLE load_test_report
    ADD jmx_content LONGTEXT NULL;

ALTER TABLE load_test_report
    ADD advanced_configuration LONGTEXT NULL;

alter table load_test_report
    add test_resource_pool_id VARCHAR(50) null;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.project_id = load_test.project_id;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.test_name = load_test.name;

UPDATE load_test_report JOIN load_test ON load_test.id = test_id
SET load_test_report.advanced_configuration = load_test.advanced_configuration;

UPDATE load_test_report JOIN load_test ON load_test.id = test_id
SET load_test_report.test_resource_pool_id = load_test.test_resource_pool_id;
--



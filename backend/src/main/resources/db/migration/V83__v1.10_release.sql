-- load_test_report
ALTER TABLE load_test_report
    ADD project_id VARCHAR(50) NULL;

ALTER TABLE load_test_report
    ADD test_name VARCHAR(64) NULL;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.project_id = load_test.project_id;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.test_name = load_test.name;
--



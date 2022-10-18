ALTER TABLE load_test_report
    MODIFY name VARCHAR(255) NOT NULL;

ALTER TABLE load_test_report
    MODIFY test_name VARCHAR(255) NULL;
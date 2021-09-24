CREATE INDEX load_test_report_test_resource_pool_id_index
    ON load_test_report (test_resource_pool_id);

ALTER TABLE test_case_review_test_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';

-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE api_test_case ADD api_change BIT(1) DEFAULT 0 NOT NULL COMMENT '接口定义参数变更标识';
ALTER TABLE api_test_case ADD ignore_api_change BIT(1) DEFAULT 0 NOT NULL COMMENT '忽略接口定义参数变更';
ALTER TABLE api_test_case ADD ignore_api_diff BIT(1) DEFAULT 0 NOT NULL COMMENT '忽略接口与用例参数不一致';
ALTER TABLE test_plan_report_bug MODIFY bug_handle_user VARCHAR(255) NULL COMMENT '缺陷处理人';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;




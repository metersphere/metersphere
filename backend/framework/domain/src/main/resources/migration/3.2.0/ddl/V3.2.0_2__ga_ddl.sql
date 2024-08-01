-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE api_test_case ADD api_change BIT(1) DEFAULT 0 NOT NULL COMMENT '接口定义参数变更标识';
ALTER TABLE api_test_case ADD ignore_api_change BIT(1) DEFAULT 0 NOT NULL COMMENT '忽略接口定义参数变更';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;




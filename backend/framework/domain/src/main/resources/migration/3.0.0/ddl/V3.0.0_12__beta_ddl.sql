-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE user_key MODIFY COLUMN description VARCHAR(1000);

ALTER TABLE api_definition_mock ADD COLUMN status_code INT(50) ;

CREATE INDEX idx_scene ON custom_field (scene);
CREATE INDEX idx_internal ON custom_field (internal);

CREATE INDEX idx_num ON test_plan(num);
ALTER TABLE test_plan_config DROP COLUMN run_mode_config;

ALTER TABLE test_plan_config ADD COLUMN test_planning BIT NOT NULL  DEFAULT 0 COMMENT '是否开启测试规划';

ALTER TABLE api_definition_mock ADD COLUMN update_user VARCHAR(50) COMMENT '更新人';

ALTER TABLE operation_history MODIFY COLUMN module VARCHAR(100);

ALTER TABLE operation_log MODIFY COLUMN module VARCHAR(100);

CREATE INDEX idx_num ON test_plan_functional_case(num);

ALTER TABLE test_resource_pool DROP COLUMN api_test;
ALTER TABLE test_resource_pool DROP COLUMN load_test;
ALTER TABLE test_resource_pool DROP COLUMN ui_test;


CREATE TABLE IF NOT EXISTS test_plan_allocation
(
    `id`           VARCHAR(50) NOT NULL COMMENT 'ID',
    `test_plan_id` VARCHAR(50) NOT NULL COMMENT '测试计划ID',
    `run_mode_config` LONGBLOB NOT NULL COMMENT '运行配置',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '测试计划配置';

CREATE INDEX idx_test_plan_id ON test_plan_allocation(test_plan_id);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;



-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE user_key MODIFY COLUMN description VARCHAR(1000);

ALTER TABLE api_definition_mock ADD COLUMN status_code INT(50) ;

CREATE INDEX idx_scene ON custom_field (scene);
CREATE INDEX idx_internal ON custom_field (internal);

CREATE INDEX idx_num ON test_plan(num);
ALTER TABLE test_plan_config DROP COLUMN run_mode_config;

ALTER TABLE test_plan_config ADD COLUMN test_planning BIT NOT NULL  DEFAULT 0 COMMENT '是否开启测试规划';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;



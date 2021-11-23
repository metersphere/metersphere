ALTER TABLE mock_expect_config ADD COLUMN expect_num varchar(50);

ALTER TABLE test_plan ADD COLUMN repeat_case TINYINT(1) DEFAULT 0 COMMENT '是否允许重复添加用例';
ALTER TABLE test_plan_api_case DROP INDEX plan_id_case_id;
ALTER TABLE test_plan_load_case DROP INDEX plan_load_case_id;
ALTER TABLE test_plan_api_scenario DROP INDEX plan_id_scenario_id;

insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+UPLOAD_JAR', 'WORKSPACE_PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_member', 'WORKSPACE_PROJECT_MANAGER:READ+UPLOAD_JAR', 'WORKSPACE_PROJECT_MANAGER');

ALTER TABLE project ADD COLUMN azure_filter_id varchar(50) NULL COMMENT 'azure 过滤需求的 parent workItem ID';

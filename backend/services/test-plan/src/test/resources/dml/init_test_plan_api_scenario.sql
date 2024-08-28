INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('wxxx_plan_1', 5000, 'wxx_project_1234', 'NONE', '1', 'qwe', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wxxx_plan_2', 10000, 'wxx_project_1234', 'NONE', '1', 'eeew', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`,
                               `case_run_mode`)
VALUES ('wxxx_plan_1', b'0', b'0', 100, 'PARALLEL'),
       ('wxxx_plan_2', b'0', b'0', 100, 'PARALLEL');


INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting)
VALUES
    ('wxx_project_1234', 5, 1, 'wx', 'wx', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');


INSERT INTO `test_plan_api_scenario`(`id`, `test_plan_id`, `api_scenario_id`, `environment_id`, `execute_user`, `last_exec_result`, `last_exec_report_id`, `create_time`, `create_user`, `pos`, `test_plan_collection_id`, `grouped`, `last_exec_time`)
VALUES
    ('wxxx_plan_scenario_1', 'wxxx_plan_1', 'wxxx_api_scenario_1', '1', 'admin', '', NULL, 1716370415311, 'admin', 64, 'wxxx_collection_2', b'0', 1716370415311),
    ('wxxx_plan_scenario_2', 'wxxx_plan_1', 'wxxx_api_scenario_2', '123', 'admin', '', NULL, 1716370415311, 'admin', 64, 'wxxx_collection_3', b'0', 1716370415311),
    ('wxxx_plan_scenario_3', 'wxxx_plan_2', 'wxxx_api_scenario_3', '1', 'admin', '', NULL, 1716370415311, 'admin', 64, 'wxxx_collection_2', b'0', 1716370415311);

INSERT INTO `api_scenario`(`id`, `name`, `priority`, `status`, `step_total`, `request_pass_rate`, `last_report_status`, `last_report_id`, `num`, `deleted`, `pos`, `version_id`, `ref_id`, `latest`, `project_id`, `module_id`, `description`, `tags`, `grouped`, `environment_id`, `create_user`, `create_time`, `delete_time`, `delete_user`, `update_user`, `update_time`)
VALUES
    ('wxxx_api_scenario_1', 'axx', 'P0', 'UNDERWAY', 3, '0.46', 'ERROR', '971160841641984', 100027, b'0', 5568, '718273150722066', '1023696881426432', b'1', 'wxx_project_1234', 'root', '', '[]', b'0', 'wx_env_123', '714940256100352', 1717489987182, NULL, NULL, '714940256100352', 1717557159805),
    ('wxxx_api_scenario_2', 'xww', 'P0', 'UNDERWAY', 3, '0.46', 'ERROR', '971160841641984', 100027, b'0', 5568, '718273150722066', '1023696881426432', b'1', 'wxx_project_1234', 'wx_scenario_module_123', '', '[]', b'0', 'wx_env_123', '714940256100352', 1717489987182, NULL, NULL, '714940256100352', 1717557159805),
    ('wxxx_api_scenario_3', 'dsa', 'P0', 'UNDERWAY', 3, '0.46', 'ERROR', '971160841641984', 100027, b'0', 5568, '718273150722066', '1023696881426432', b'1', 'wxx_project_1234', 'wx_scenario_module_123', '', '[]', b'0', 'wx_env_123', '714940256100352', 1717489987182, NULL, NULL, '714940256100352', 1717557159805);



INSERT INTO `test_plan_collection`(`id`, `test_plan_id`, `name`, `type`, `environment_id`, `test_resource_pool_id`, `pos`, `create_user`, `create_time`, `parent_id`)
VALUES
    ('wxxx_collection_1', 'wxxx_plan_1', 'wxxx_wx_1', 'SCENARIO', 'NONE', 'NONE', 1, 'admin', 1716370415311, 'NONE'),
    ('wxxx_collection_2', 'wxxx_plan_1', 'wxxx_wx_2', 'SCENARIO', 'wx_env_123', 'NONE', 2, 'admin', 1716370415311, 'wxxx_collection_1'),
    ('wxxx_collection_3', 'wxxx_plan_2', 'wxxx_wx_3', 'SCENARIO', 'NONE', 'NONE', 3, 'admin', 1716370415311, 'wxxx_collection_1');



INSERT INTO `environment`(`id`, `name`, `project_id`, `create_user`, `update_user`, `create_time`, `update_time`, `mock`, `description`, `pos`)
VALUES
    ('wx_env_123', 'Mock环境', 'wxx_project_1234', 'admin', 'admin', 1716175907000, 1716175907000, b'1', NULL, 64),
    ('wx_env_223', '测试环境', 'wxx_project_1234', 'admin', 'admin', 1716175907000, 1716175907000, b'1', NULL, 128);


INSERT INTO `api_scenario_module`(`id`, `name`, `pos`, `create_time`, `update_time`, `update_user`, `create_user`, `project_id`, `parent_id`)
VALUES ('wx_scenario_module_123', '测试CSV', 64, 1716196253511, 1716196253511, '714940256100352', '714940256100352', '718255970852864', 'NONE');

INSERT INTO bug_relation_case (id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time) VALUES
('bug_relate_5', 'wxxx_api_scenario_1', 'bug_5', 'FUNCTIONAL', 'wxxx_plan_1', 'wxxx_plan_scenario_1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bug_relate_6', 'wxxx_api_scenario_1', 'bug_6', 'FUNCTIONAL', 'wxxx_plan_1', 'wxxx_plan_scenario_1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('bug_5', 100001, 'oasis', 'admin', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

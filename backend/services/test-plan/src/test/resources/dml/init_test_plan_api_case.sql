INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('wxxx_1', 5000, 'wxx_1234', 'NONE', '1', 'qwe', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wxxx_2', 10000, 'wxx_1234', 'NONE', '1', 'eeew', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`,
                               `case_run_mode`)
VALUES ('wxxx_1', b'0', b'1', 100, 'PARALLEL'),
       ('wxxx_2', b'0', b'1', 100, 'PARALLEL');


INSERT INTO `api_definition`(`id`, `name`, `protocol`, `method`, `path`, `status`, `num`, `tags`, `pos`, `project_id`, `module_id`, `latest`, `version_id`, `ref_id`, `description`, `create_time`, `create_user`, `update_time`, `update_user`, `delete_user`, `delete_time`, `deleted`)
VALUES
    ('wxxx_api_1', '2222', 'HTTP', 'GET', '/111', 'DEPRECATED', 100003, '[]', 192, 'wxx_1234', 'root', b'1', '100844458962059498', '1086025445195776', '', 1716370415311, 'admin', 1716455838628, 'admin', NULL, NULL, b'0'),
    ('wxxx_api_2', '3333', 'HTTP', 'GET', '/111', 'DEPRECATED', 100003, '[]', 192, 'wxx_1234', '123', b'1', '100844458962059498', '1086025445195776', '', 1716370415311, 'admin', 1716455838628, 'admin', NULL, NULL, b'0'),
    ('wxxx_api_3', '4444', 'HTTP', 'GET', '/111', 'DEPRECATED', 100003, '[]', 192, 'wxx_1234', 'root', b'1', '100844458962059498', '1086025445195776', '', 1716370415311, 'admin', 1716455838628, 'admin', NULL, NULL, b'0');


INSERT INTO `api_test_case`(`id`, `name`, `priority`, `num`, `tags`, `status`, `last_report_status`, `last_report_id`, `pos`, `project_id`, `api_definition_id`, `version_id`, `environment_id`, `create_time`, `create_user`, `update_time`, `update_user`, `delete_time`, `delete_user`, `deleted`)
VALUES
    ('wxxx_api_case_1', '231', 'P0', 100055001, '[]', 'PROCESSING', 'SUCCESS', '1130899263537153', 64, 'wxx_1234', 'wxxx_api_1', '899658209591296', '123', 1716199600948, 'admin', 1716199600948, 'admin', NULL, NULL, b'0'),
    ('wxxx_api_case_2', '232', 'P0', 100055001, '[]', 'PROCESSING', 'SUCCESS', '1130899263537153', 64, 'wxx_1234', 'wxxx_api_1', '899658209591296', '123', 1716199600948, 'admin', 1716199600948, 'admin', NULL, NULL, b'0'),
    ('wxxx_api_case_3', '233', 'P0', 100055001, '[]', 'PROCESSING', 'SUCCESS', '1130899263537153', 64, 'wxx_1234', 'wxxx_api_2', '899658209591296', '123', 1716199600948, 'admin', 1716199600948, 'admin', NULL, NULL, b'0');

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting)
VALUES
    ('wxx_1234', 4, 1, 'wx', 'wx', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');


INSERT INTO `test_plan_api_case`(`id`, `test_plan_id`, `api_case_id`, `environment_id`, `last_exec_result`, `last_exec_report_id`, `execute_user`, `create_time`, `create_user`, `pos`, `test_plan_collection_id`, `last_exec_time`)
VALUES
    ('wxxx_1', 'wxxx_1', 'wxxx_api_case_1', '1', NULL, NULL, 'admin', 1716370415311, 'admin', 4096, 'wxxx_2', 1716370415311),
    ('wxxx_2', 'wxxx_1', 'wxxx_api_case_2', '123', NULL, NULL, 'admin', 1716370415311, 'admin', 4096, 'wxxx_3', 1716370415311),
    ('wxxx_3', 'wxxx_2', 'wxxx_api_case_3', '1', NULL, NULL, 'admin', 1716370415311, 'admin', 8192, 'wxxx_2', 1716370415311);

INSERT INTO `test_plan_collection`(`id`, `test_plan_id`, `name`, `type`, `environment_id`, `test_resource_pool_id`, `pos`, `create_user`, `create_time`, `parent_id`)
VALUES
    ('wxxx_1', 'wxxx_1', 'wxxx_1', 'API', 'NONE', 'NONE', 1, 'admin', 1716370415311, 'NONE'),
    ('wxxx_2', 'wxxx_1', 'wxxx_2', 'API', '123', 'NONE', 2, 'admin', 1716370415311, 'wxxx_1'),
    ('wxxx_3', 'wxxx_2', 'wxxx_3', 'API', 'NONE', 'NONE', 3, 'admin', 1716370415311, 'wxxx_1');



INSERT INTO `environment`(`id`, `name`, `project_id`, `create_user`, `update_user`, `create_time`, `update_time`, `mock`, `description`, `pos`)
VALUES
    ('123', 'Mock环境', 'wxx_1234', 'admin', 'admin', 1716175907000, 1716175907000, b'1', NULL, 64),
    ('223', '测试环境', 'wxx_1234', 'admin', 'admin', 1716175907000, 1716175907000, b'1', NULL, 128);

INSERT INTO `api_definition_module`(`id`, `name`, `parent_id`, `project_id`, `pos`, `create_time`, `update_time`, `update_user`, `create_user`)
VALUES ('123', 'Halo', 'NONE', 'wxx_1234', 384, 1716280762025, 1716280762025, '805048669970432', '805048669970432');

INSERT INTO bug_relation_case (id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time) VALUES
('bug_relate_3', 'wxxx_api_case_1', 'bug_3', 'FUNCTIONAL', 'wxxx_1', 'wxxx_1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bug_relate_4', 'wxxx_api_case_1', 'bug_4', 'FUNCTIONAL', 'wxxx_1', 'wxxx_1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('bug_3', 100001, 'oasis', 'admin', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

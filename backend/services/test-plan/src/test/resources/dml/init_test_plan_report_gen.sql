-- 计划的测试数据
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`) VALUES
('plan_id_for_gen_report', 100001, '100001100001', 'NONE', '1', 'gen-report-plan', 'PREPARED', 'TEST_PLAN', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11'),
('plan_id_for_gen_report_1', 100001, '100001100001', 'NONE', '1', 'gen-report-plan-1', 'PREPARED', 'GROUP', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');
INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`,
                               `case_run_mode`)
VALUES ('plan_id_for_gen_report', b'0', b'0', 100.00, 'PARALLEL'),
       ('plan_id_for_gen_report_1', b'0', b'0', 0.00, 'PARALLEL');

-- 计划关联用例执行的测试数据
INSERT INTO `test_plan_functional_case` (`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`, `test_plan_collection_id`) VALUES
('plan_id_for_gen_report_case_1', 'plan_id_for_gen_report', 'f1_gen', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'PENDING', 1, '123'),
('plan_id_for_gen_report_case_2', 'plan_id_for_gen_report', 'f2_gen', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'SUCCESS', 2, '123'),
('plan_id_for_gen_report_case_3', 'plan_id_for_gen_report', 'f3_gen', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'ERROR', 3, '123'),
('plan_id_for_gen_report_case_4', 'plan_id_for_gen_report', 'f4_gen', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'BLOCKED', 4, '123'),
('plan_id_for_gen_report_case_5', 'plan_id_for_gen_report', 'f5_gen', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'FAKE_ERROR', 5, '123');

INSERT INTO `test_plan_api_case`(`id`, `test_plan_id`, `api_case_id`, `environment_id`, `last_exec_result`, `last_exec_report_id`, `execute_user`, `create_time`, `create_user`, `pos`, `test_plan_collection_id`, `last_exec_time`) VALUES
('plan_id_for_gen_report_api_case_1', 'plan_id_for_gen_report', 'a1_gen', '1', 'SUCCESS', NULL, 'admin', CURRENT_TIMESTAMP, 'admin', 1, '123', CURRENT_TIMESTAMP);
INSERT INTO `test_plan_api_scenario`(id, test_plan_id, api_scenario_id, environment_id, execute_user, last_exec_result, last_exec_report_id, create_time, create_user, pos, test_plan_collection_id, grouped, last_exec_time)
VALUES ('plan_id_for_gen_report_api_scenario_case_1', 'plan_id_for_gen_report', 'as_1', '1','admin', 'SUCCESS', NULL, CURRENT_TIMESTAMP, 'admin',  1, '123', b'0', CURRENT_TIMESTAMP);


-- 计划关联缺陷的测试数据
INSERT INTO `bug_relation_case` (`id`, `case_id`, `bug_id`, `case_type`, `test_plan_id`, `test_plan_case_id`, `create_user`, `create_time`, `update_time`) VALUES
('test-plan-bug-relate-case-1', 'f1', 'test-plan-bug-for-gen', 'FUNCTIONAL', 'plan_id_for_gen_report', 'f1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 缺陷的测试数据
INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time, update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUES
('test-plan-bug-for-gen', 100000, 'default-bug-gen', 'oasis', 'admin', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '100001100001', 'bug-template-id', 'Local', 'new', '["default-tag"]', null, 0, 5000);

-- 用例的测试数据
INSERT INTO functional_case (id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, ai_create, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('f1_gen', 100001, 'TEST_MODULE_ID', '100001100001', '100001', 'functional_case_gen', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'PENDING', b'0', b'0', b'1', b'0', 'admin', 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO functional_case_module (id, project_id, name, parent_id, pos, create_user, create_time, update_user, update_time) VALUES
('TEST_MODULE_ID', '100001100001', 'test_module_gen_1', 'NONE', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('TEST_MODULE_ID_1', '100001100001', 'test_module_gen_2', 'TEST_MODULE_ID', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('TEST_MODULE_ID_2', '100001100001', 'test_module_gen_3', '', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted) VALUES
    ('a1_gen', 'api_case_gen', 'P0', 1001, null, 'Underway', 'PENDING', null, 100, 'project-associate-case-test', 'oasis_ac_definition', 'oasis_ac_version_id', 'oasis_ac_env_id', 1698058347559, 'admin', 1698058347559, 'admin', null, null, false);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time, deleted) VALUES
    ('as_1', 'api_scenario_gen', 'P0', 'Underway',  'PENDING', null, 1000001, 1, 'oasis_as_version_id', 'as-rid', 'test-associate-pro', 'root', null, null, 'admin',  UNIX_TIMESTAMP() * 1000, null, null, 'admin', UNIX_TIMESTAMP() * 1000, false);
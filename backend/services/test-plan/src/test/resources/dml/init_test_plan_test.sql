INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time,
                     module_setting)
VALUES ('songtianyang-fix-wx', 97, 1, 'songtianyang-fix-wx', 'songtianyang-fix-wx', 'admin', 'admin',
        unix_timestamp() * 1000, unix_timestamp() * 1000,
        '["bugManagement","caseManagement","apiTest","testPlan"]');

INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('wx_test_plan_id_1', 5000, 'songtianyang-fix-wx', 'NONE', '1', '测试一下计划', 'PREPARED', 'TEST_PLAN', NULL,
        1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
       ('wx_test_plan_id_2', 10000, 'songtianyang-fix-wx', 'NONE', '1', '测试一下组', 'PREPARED', 'GROUP', NULL,
        1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
       ('wx_test_plan_id_3', 15000, 'songtianyang-fix-wx', 'NONE', '1', '测试一下组2', 'PREPARED', 'GROUP', NULL,
        1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
       ('wx_test_plan_id_4', 20000, 'songtianyang-fix-wx', 'wx_test_plan_id_3', '1', '测试一下计划2', 'PREPARED',
        'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000,
        1714980158000, '11'),
       ('wx_test_plan_id_5', 25000, 'songtianyang-fix-wx', 'NONE', '1', '测试一下组3', 'PREPARED', 'GROUP', NULL,
        1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
       ('wx_test_plan_id_6', 30000, 'songtianyang-fix-wx', 'wx_test_plan_id_5', '1', '测试组3下计划', 'COMPLETED',
        'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000,
        1714980158000, '11'),
       ('wx_test_plan_id_7', 30000, 'songtianyang-fix-wx', 'NONE', '1', '测试组4下计划', 'COMPLETED', 'TEST_PLAN', NULL,
        1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
       ('oasis_test_plan_id_1', 30000, 'songtianyang-fix-wx', 'NONE', '1', '计划组-复制', 'COMPLETED', 'GROUP', NULL,
        1714980158000, 'admin', 1714980158000, 'admin', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
       ('oasis_test_plan_id_2', 30000, 'songtianyang-fix-wx', 'oasis_test_plan_id_1', '1', '计划-复制', 'COMPLETED', 'TEST_PLAN', NULL,
        1714980158000, 'admin', 1714980158000, 'admin', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');

-- 定时任务(复制)
INSERT INTO `schedule` (`id`, `key`, `type`, `value`, `job`, `resource_type`, `enable`, `resource_id`, `create_user`, `create_time`, `update_time`, `project_id`, `name`, `config`, `num`) VALUE
('schedule-id-oasis', 'oasis_test_plan_id_2', 'CRON', '0 0 0/6 * * ?', 'io.metersphere.plan.job.TestPlanScheduleJob', 'TEST_PLAN', true, 'oasis_test_plan_id_2', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'songtianyang-fix-wx', '计划-复制', '{"runMode":"SERIAL"}', 123);

INSERT INTO `test_plan_functional_case`(`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`, `test_plan_collection_id`) VALUES
    ('wx_tpfc_1', 'wx_test_plan_id_4', 'wx_fc_1', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123'),
    ('oasis_1', 'wx_test_plan_id_7', 'oasis_fc_1', 1714980158000, 'admin', NULL, NULL, 'UN_EXECUTED', 1, '123'),
    ('oasis_2', 'wx_test_plan_id_7', 'oasis_fc_2', 1714980158000, 'admin', NULL, NULL, 'PASSED', 1, '123'),
    ('oasis_3', 'wx_test_plan_id_7', 'oasis_fc_3', 1714980158000, 'admin', NULL, NULL, 'FAILED', 1, '123'),
    ('oasis_4', 'wx_test_plan_id_7', 'oasis_fc_4', 1714980158000, 'admin', NULL, NULL, 'BLOCKED', 1, '123'),
    ('oasis_5', 'wx_test_plan_id_7', 'oasis_fc_5', 1714980158000, 'admin', NULL, NULL, 'FAKE_ERROR', 1, '123');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time) VALUES
  ('oasis_fc_1', 10001, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'PENDING', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_2', 10002, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'SUCCESS', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_3', 10003, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'ERROR', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_4', 10004, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'BLOCKED', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_5', 10005, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'FAKE_ERROR', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

INSERT INTO test_plan_api_case(`id`, `test_plan_id`, `api_case_id`, `environment_id`, `last_exec_result`, `last_exec_report_id`, `execute_user`, `create_time`, `create_user`, `pos`, `test_plan_collection_id`) VALUES
    ('oasis_1', 'wx_test_plan_id_7', 'oasis_ac_1', '1', 'PASSED', NULL, 'admin', 1716370415311, 'admin', 1, '123');

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted) VALUES
    ('oasis_ac_1', 'oasis_ac', 'P0', 1001, null, 'Underway', 'PENDING', null, 100, 'project-associate-case-test', 'oasis_ac_definition', 'oasis_ac_version_id', 'oasis_ac_env_id', 1698058347559, 'admin', 1698058347559, 'admin', null, null, false);

INSERT INTO test_plan_api_scenario (id, test_plan_id, api_scenario_id, environment_id, execute_user, last_exec_result, last_exec_report_id, create_time, create_user, pos, test_plan_collection_id, grouped) VALUES
    ('oasis_1', 'wx_test_plan_id_7', 'oasis_as_1', '1', 'admin', 'PASSED', NULL, 1716370415311, 'admin', 1, '123', false);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time, deleted) VALUES
    ('oasis_as_1', 'oasis_as', 'P0', 'Underway',  'PENDING', null, 1000001, 1, 'oasis_as_version_id', 'as-rid', 'test-associate-pro', 'root', null, null, 'admin',  UNIX_TIMESTAMP() * 1000, null, null, 'admin', UNIX_TIMESTAMP() * 1000, false);


INSERT INTO `test_plan_module`(`id`, `project_id`, `name`, `parent_id`, `pos`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES ('1', 'songtianyang-fix-wx', 'wx_测试模块名称', 'ROOT', 1, 1714980158000, 1714980158000, 'admin', 'admin');


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`,
                               `case_run_mode`)
VALUES ('wx_test_plan_id_1', b'0', b'0', 100, 'PARALLEL'),
       ('wx_test_plan_id_4', b'0', b'0', 100, 'PARALLEL'),
       ('wx_test_plan_id_7', b'0', b'0', 100, 'PARALLEL');


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('my_test_1', 1, '1', 'songtianyang-fix-wx', '100001', '1111', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0',
        'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559,
        1698058347559, NULL),
       ('my_test_2', 2, '1', 'songtianyang-fix-wx', '100001', '2222', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0',
        'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559,
        1698058347559, NULL),
       ('my_test_3', 3, 'root', 'songtianyang-fix-wx', '100001', '3333', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0',
        'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559,
        1698058347559, NULL);

-- 初始化计划默认节点
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('init_plan_id', 5000, 'init_project', 'NONE', '1', '测试一下计划', 'PREPARED', 'TEST_PLAN', NULL,
        unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000, unix_timestamp() * 1000, unix_timestamp() * 1000, '11');

-- 初始化功能集 (删除使用)
INSERT INTO `test_plan_collection`(`id`, `test_plan_id`, `name`, `type`, `environment_id`, `test_resource_pool_id`, `pos`, `create_user`, `create_time`) VALUES
('init_collection-delete-1', 'init_plan_id', '功能用例点', 'FUNCTIONAL', '1', '1', 1, 'admin', unix_timestamp() * 1000),
('init_collection-delete-2', 'init_plan_id', '接口功能点', 'API', '1', '1', 1, 'admin', unix_timestamp() * 1000),
('init_collection-delete-3', 'init_plan_id', '场景功能点', 'SCENARIO', '1', '1', 1, 'admin', unix_timestamp() * 1000),
('init_collection-delete-4', 'init_plan_id', '未知的功能点', 'UNKNOWN', '1', '1', 1, 'admin', unix_timestamp() * 1000);


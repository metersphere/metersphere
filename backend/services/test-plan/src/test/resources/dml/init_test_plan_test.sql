INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('wx_test_plan_id_1', 5000, '123', 'NONE', '1', '测试一下计划', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_2', 10000, '123', 'NONE', '1', '测试一下组', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_3', 15000, '123', 'NONE', '1', '测试一下组2', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_4', 20000, '123', 'wx_test_plan_id_3', '1', '测试一下计划2', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_5', 25000, '123', 'NONE', '1', '测试一下组3', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_6', 30000, '123', 'wx_test_plan_id_5', '1', '测试组3下计划', 'COMPLETED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_7', 30000, '123', 'NONE', '1', '测试组4下计划', 'COMPLETED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');


INSERT INTO `test_plan_functional_case`(`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`) VALUES
    ('wx_tpfc_1', 'wx_test_plan_id_4', 'wx_fc_1', 1714980158000, 'admin', NULL, NULL, NULL, 1),
    ('oasis_1', 'wx_test_plan_id_7', 'oasis_fc_1', 1714980158000, 'admin', NULL, NULL, 'UN_EXECUTED', 1),
    ('oasis_2', 'wx_test_plan_id_7', 'oasis_fc_2', 1714980158000, 'admin', NULL, NULL, 'PASSED', 1),
    ('oasis_3', 'wx_test_plan_id_7', 'oasis_fc_3', 1714980158000, 'admin', NULL, NULL, 'FAILED', 1),
    ('oasis_4', 'wx_test_plan_id_7', 'oasis_fc_4', 1714980158000, 'admin', NULL, NULL, 'BLOCKED', 1),
    ('oasis_5', 'wx_test_plan_id_7', 'oasis_fc_5', 1714980158000, 'admin', NULL, NULL, 'FAKE_ERROR', 1);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time) VALUES
  ('oasis_fc_1', 10001, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'PENDING', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_2', 10002, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'SUCCESS', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_3', 10003, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'ERROR', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_4', 10004, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'BLOCKED', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
  ('oasis_fc_5', 10005, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'FAKE_ERROR', b'0', b'0', b'1', 'admin', 'admin', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);


INSERT INTO `test_plan_module`(`id`, `project_id`, `name`, `parent_id`, `pos`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
    ('1', '123', 'wx_测试模块名称', 'ROOT', 1, 1714980158000, 1714980158000, 'admin', 'admin');


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`, `test_planning`)
VALUES
    ('wx_test_plan_id_1', b'0', b'0', 100, b'0'),
    ('wx_test_plan_id_4', b'0', b'0', 100, b'0'),
    ('wx_test_plan_id_7', b'0', b'0', 100, b'0');


INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting)
VALUES
    ('123', 2, 1, 'wx', 'wx', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');


INSERT INTO `test_plan_allocation`(`id`, `test_plan_id`, `run_mode_config`)
VALUES
    ('1', 'wx_test_plan_id_1', '111');


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES
    ('my_test_1', 1, '1', '123', '100001', '1111', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('my_test_2', 2, '1', '123', '100001', '2222', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('my_test_3', 3, 'root', '123', '100001', '3333', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);


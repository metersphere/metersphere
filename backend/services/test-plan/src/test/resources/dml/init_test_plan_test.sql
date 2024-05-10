INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('wx_test_plan_id_1', 5000, '123', 'NONE', '1', '测试一下计划', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_2', 10000, '123', 'NONE', '1', '测试一下组', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_3', 15000, '123', 'NONE', '1', '测试一下组2', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_4', 20000, '123', 'wx_test_plan_id_3', '1', '测试一下计划2', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_5', 25000, '123', 'NONE', '1', '测试一下组3', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_6', 30000, '123', 'wx_test_plan_id_5', '1', '测试组3下计划', 'COMPLETED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_7', 30000, '123', 'NONE', '1', '测试组4下计划', 'COMPLETED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');


INSERT INTO `test_plan_functional_case`(`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`)
VALUES ('wx_tpfc_1', 'wx_test_plan_id_4', 'wx_fc_1', 1714980158000, 'admin', NULL, NULL, NULL, 1);


INSERT INTO `test_plan_module`(`id`, `project_id`, `name`, `parent_id`, `pos`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
    ('1', '123', 'wx_测试模块名称', 'ROOT', 1, 1714980158000, 1714980158000, 'admin', 'admin');


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`, `test_planning`)
VALUES
    ('wx_test_plan_id_1', b'0', b'0', 100, b'0'),
    ('wx_test_plan_id_4', b'0', b'0', 100, b'0');


INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting)
VALUES
    ('123', 2, 1, 'wx', 'wx', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');


INSERT INTO `test_plan_allocation`(`id`, `test_plan_id`, `run_mode_config`)
VALUES
    ('1', 'wx_test_plan_id_1', '111');

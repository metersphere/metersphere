INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('wxx_1', 5000, '123', 'NONE', '1', 'qwe', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wxx_2', 10000, '123', 'NONE', '1', 'eeew', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`)
VALUES
    ('wxx_1', b'0', b'0', 100),
    ('wxx_2', b'0', b'0', 100);

INSERT INTO `test_plan_functional_case`(`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`)
VALUES ('wxx_tpfc_1', 'wxx_1', 'wxx_test_1', 1714980158000, 'admin', NULL, NULL, NULL, 1);


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES
    ('wxx_test_1', 1, '1', '123', '100001', '1111', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('wxx_test_2', 2, '1', '123', '100001', '2222', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('wxx_test_3', 3, 'root', '123', '100001', '3333', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);


INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('wxx_1', 5000, 'wx_1234', 'NONE', '1', 'qwe', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wxx_2', 10000, 'wx_1234', 'NONE', '1', 'eeew', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`,
                               `case_run_mode`)
VALUES ('wxx_1', b'0', b'0', 100, 'PARALLEL'),
       ('wxx_2', b'0', b'0', 100, 'PARALLEL');

INSERT INTO `test_plan_functional_case`(`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`, `test_plan_collection_id`)
VALUES ('wxx_tpfc_1', 'wxx_1', 'wxx_test_1', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123');


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES
    ('wxx_test_1', 1, '1', 'wx_1234', '100001', '1111', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('wxx_test_2', 2, '1', 'wx_1234', '100001', '2222', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('wxx_test_3', 3, 'root', 'wx_1234', '100001', '3333', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO `api_definition`(`id`, `name`, `protocol`, `method`, `path`, `status`, `num`, `tags`, `pos`, `project_id`, `module_id`, `latest`, `version_id`, `ref_id`, `description`, `create_time`, `create_user`, `update_time`, `update_user`, `delete_user`, `delete_time`, `deleted`)
VALUES
    ('wxx_api_1', '2222', 'HTTP', 'GET', '/111', 'DEPRECATED', 100003, '[]', 192, 'wx_1234', 'root', b'1', '100844458962059498', '1086025445195776', '', 1716370415311, 'admin', 1716455838628, 'admin', NULL, NULL, b'0'),
    ('wxx_api_2', '3333', 'HTTP', 'GET', '/111', 'DEPRECATED', 100003, '[]', 192, 'wx_1234', 'root', b'1', '100844458962059498', '1086025445195776', '', 1716370415311, 'admin', 1716455838628, 'admin', NULL, NULL, b'0'),
    ('wxx_api_3', '4444', 'HTTP', 'GET', '/111', 'DEPRECATED', 100003, '[]', 192, 'wx_1234', 'root', b'1', '100844458962059498', '1086025445195776', '', 1716370415311, 'admin', 1716455838628, 'admin', NULL, NULL, b'0');


INSERT INTO `api_test_case`(`id`, `name`, `priority`, `num`, `tags`, `status`, `last_report_status`, `last_report_id`, `pos`, `project_id`, `api_definition_id`, `version_id`, `environment_id`, `create_time`, `create_user`, `update_time`, `update_user`, `delete_time`, `delete_user`, `deleted`)
VALUES
    ('wxx_api_case_1', '231', 'P0', 100055001, '[]', 'PROCESSING', 'SUCCESS', '1130899263537153', 64, 'wx_1234', 'wxx_api_1', '899658209591296', '899606669983745', 1716199600948, 'admin', 1716199600948, 'admin', NULL, NULL, b'0'),
    ('wxx_api_case_2', '232', 'P0', 100055001, '[]', 'PROCESSING', 'SUCCESS', '1130899263537153', 64, 'wx_1234', 'wxx_api_1', '899658209591296', '899606669983745', 1716199600948, 'admin', 1716199600948, 'admin', NULL, NULL, b'0'),
    ('wxx_api_case_3', '233', 'P0', 100055001, '[]', 'PROCESSING', 'SUCCESS', '1130899263537153', 64, 'wx_1234', 'wxx_api_2', '899658209591296', '899606669983745', 1716199600948, 'admin', 1716199600948, 'admin', NULL, NULL, b'0');

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting)
VALUES
    ('wx_1234', 3, 1, 'wx', 'wx', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');
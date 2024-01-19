INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-case-minder-test', null, '100001', '用例脑图项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', 1, 'TEST_MINDER_MODULE_ID_GYQ', 'project-case-minder-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_2', 2, 'TEST_MINDER_MODULE_ID_GYQ', 'project-case-minder-test', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 5000, 'v1.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_2', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_3', 3, 'TEST_MINDER_MODULE_ID_GYQ2', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 25000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_3', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_4', 4, 'TEST_MINDER_MODULE_ID_GYQ3', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 40000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_4', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_5', 5, 'TEST_MINDER_MODULE_ID_GYQ4', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 45000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_5', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('TEST_FUNCTIONAL_MINDER_CASE_ID_6', 6, 'TEST_MINDER_MODULE_ID_GYQ5', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_2', NULL, NULL, NULL, NULL, NULL);
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_3', NULL, NULL, NULL, NULL, NULL);


INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'custom_field_minder_gyq_id_3', 'P0');



INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user)
VALUES
    ('TEST_MINDER_MODULE_ID_GYQ', 'project-case-minder-test', '测试所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ2', 'project-case-minder-test', '测试所属模块2', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ3', 'project-case-minder-test', '测试所属模块3', 'TEST_MINDER_MODULE_ID_GYQ2', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ4', 'project-case-minder-test', '测试所属模块4', 'TEST_MINDER_MODULE_ID_GYQ3', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ5', 'project-case-minder-test', '测试所属模块5', 'TEST_MINDER_MODULE_ID_GYQ4', 0, 1669174143999, 1669174143999, 'admin', 'admin');


INSERT INTO custom_field (id, name, scene, type, remark, internal, scope_type, create_time, update_time, create_user, scope_id) VALUE
    ('custom_field_minder_gyq_id_3', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 0, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001');






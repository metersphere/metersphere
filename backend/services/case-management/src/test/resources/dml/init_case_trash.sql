
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-case-trash-test', null, 'organization-case-trash-test', 'organization-case-trash-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-case-trash-test', null, 'organization-case-trash-test', '用例回收项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-case-trash-test-1', null, 'organization-case-trash-test', '用例回收项目1', '系统默认创建的项目1', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-case-trash-test-2', null, 'organization-case-trash-test', '用例回收项目2', '系统默认创建的项目2', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-case-trash-test-3', null, 'organization-case-trash-test', '用例回收项目3', '系统默认创建的项目3', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, ref_id, scene)
VALUES ('test_template_case_trash_id', 'functional_case_trash_default', '', b'0', 1696992836000, 1696992836000, 'admin', 'PROJECT', 'project-case-trash-test', b'0', NULL, 'FUNCTIONAL');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID', 1, 'Trash_TEST_MOUDLE_ID', 'project-case-trash-test', '100001', '回收站测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_1', 2, 'Trash_TEST_MOUDLE_ID', 'project-case-trash-test', '100001', '测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_1', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_2', 3, 'Trash_TEST_MOUDLE_ID', 'project-case-trash-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v2.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_1', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_3', 3, 'Trash_TEST_MOUDLE_ID', 'project-case-trash-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_1', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_4', 4, 'Trash_TEST_MOUDLE_ID', 'project-case-trash-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_5', 1, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本5', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_5', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_6', 2, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本6', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_5', 'UN_EXECUTED',true, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_7', 3, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本7', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_7', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_8', 4, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本8', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_7', 'UN_EXECUTED',true, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_9', 5, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本9', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_9', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_a', 6, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本a', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_9', 'UN_EXECUTED',true, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_b', 7, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本b', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_b', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_c', 8, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-1', '100001', 'copy_测试多版本c', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_b', 'UN_EXECUTED',true, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_d', 7, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-2', '100001', 'copy_测试多版本d', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_d', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_e', 8, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-2', '100001', 'copy_测试多版本e', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_d', 'UN_EXECUTED',true, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_f', 7, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-2', '100001', 'copy_测试多版本f', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_f', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_g', 8, 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test-2', '100001', 'copy_测试多版本g', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_f', 'UN_EXECUTED',true, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_GYQ', 100, 'Trash_TEST_MOUDLE_ID', 'project-case-trash-test', '100001', '回收站测信', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_GYQ', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('unTrash_TEST_FUNCTIONAL_CASE_ID', 100, 'Trash_TEST_MOUDLE_ID', 'project-case-trash-test', '100001', '回收站测信', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'unTrash_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED', false, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('Gyq_Review_Case_Id','Gyq_review_id', 'Trash_TEST_FUNCTIONAL_CASE_ID', 'PASS', 1698058347559, 'admin',1698058347559, 0);

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('Gyq_Review_Case_Id2','Gyq_review_id1', 'Trash_TEST_FUNCTIONAL_CASE_ID_1', 'PASS', 1698058347559, 'admin',1698058347559, 0);

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('Gyq_Review_Case_Id3','Gyq_review_id1', 'unTrash_TEST_FUNCTIONAL_CASE_ID', 'PASS', 1698058347559, 'admin',1698058347559, 0);

INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('Gyq_review_id', 10001, '用例评审1', 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test', 'COMPLETE' , 'SINGLE', 0, 1698058347559, 1698058347559, 1, 100.00, null, null, 1698058347559, 'admin', 1698058347559, 'admin');

INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('Gyq_review_id1', 10001, '用例评审2', 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test', 'COMPLETE' , 'SINGLE', 0, 1698058347559, 1698058347559, 1, 100.00, null, null, 1698058347559, 'admin', 1698058347559, 'admin');

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID', 'gyq_custom_id1', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID', 'gyq_custom_id2', '33');

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_5', 'gyq_custom_id1', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_5', 'gyq_custom_id2', '33');


INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id1', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_trash_id');

INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id2', 'level', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_trash_id');

INSERT INTO functional_case_comment(id, case_id, create_user, parent_id, resource_id, notifier, content, reply_user, create_time, update_time)
VALUES ('trash_comment_id', 'Trash_TEST_FUNCTIONAL_CASE_ID', 'gyq', null, null, 'gyq','你好', null, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('Trash_TEST_MOUDLE_ID', '100001100001', '测试回收站所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID', 1, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', '回收站测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_1', 2, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', '测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_1', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_2', 3, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v2.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_1', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_3', 3, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_1', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_4', 4, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED',true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID', 'gyq_custom_id1', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID', 'gyq_custom_id2', '33');


INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id1', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001');

INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id2', 'level', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001');

INSERT INTO functional_case_comment(id, case_id, create_user, status, parent_id, resource_id, notifier, content, reply_user, create_time, update_time)
VALUES ('trash_comment_id', 'Trash_TEST_FUNCTIONAL_CASE_ID', 'gyq', null, null, null, 'gyq','你好', null, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);


INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID', 'STEP', '1111', NULL, NULL, 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_1', 'STEP', '1111', NULL, NULL, 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_2', 'STEP', '1111', NULL, NULL, 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_3', 'STEP', '1111', NULL, NULL, 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('Trash_TEST_FUNCTIONAL_CASE_ID_4', 'STEP', '1111', NULL, NULL, 'TEST');
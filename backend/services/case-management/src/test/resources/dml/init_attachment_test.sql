INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ATTACHMENT_ID', 1, 'TEST_MODULE_ID', 'WX_TEST_PROJECT_ID', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1', 2, 'TEST_MODULE_ID', 'WX_TEST_PROJECT_ID', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ATTACHMENT_ID', 'STEP', '1111', '', '', 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1', 'STEP', '1111', '', '', '1111');


INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_CASE_ATTACHMENT_ID', '100548878725546079', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_CASE_ATTACHMENT_ID', 'TEST_FIELD_ID', '["222","333"]');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_CASE_ATTACHMENT_ID', 'TEST_FIELD_ID_1', '["222","333"]');


INSERT INTO functional_case_attachment(id, case_id, file_id, file_name, size, local, create_user, create_time) VALUES ('TEST_ATTACHMENT_ID', 'TEST_FUNCTIONAL_CASE_ATTACHMENT_ID', 'TEST_ATTACHMENT_FILE_ID', '测试', 1, b'1', 'admin', 1698058347559);
INSERT INTO functional_case_attachment(id, case_id, file_id, file_name, size, local, create_user, create_time) VALUES ('TEST_ATTACHMENT_ID_1', 'TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1', 'TEST_ATTACHMENT_FILE_ID_1', '测试1', 1, b'0', 'admin', 1698058347559);


INSERT INTO file_association(id, source_type, source_id, file_id, file_ref_id, file_version, create_time, update_user, update_time, create_user) VALUES ('wx_test_file_association', 'functional_case', 'WX_TEST_FUNCTIONAL_CASE_ID', 'wx_file_id', '1', '1', 1698983271536, 'admin', 1698983271536, 'admin');
INSERT INTO file_metadata(id, name, type, size, create_time, update_time, project_id, storage, create_user, update_user, tags, description, module_id, path, latest, ref_id, file_version) VALUES ('wx_file_id', 'formItem', 'ts', 2502, 1698058347559, 1698058347559, '100001100001', 'MINIO', 'admin', 'admin', NULL, NULL, 'root', '100001100001/1127016598347779', b'1', '1127016598347779', '1127016598347779');


INSERT INTO file_association(id, source_type, source_id, file_id, file_ref_id, file_version, create_time, update_user, update_time, create_user) VALUES ('wx_test_file_association_1', 'functional_case', 'WX_TEST_FUNCTIONAL_CASE_ID', 'wx_file_id_1', '1', '1', 1698983271536, 'admin', 1698983271536, 'admin');
INSERT INTO file_metadata(id, name, type, size, create_time, update_time, project_id, storage, create_user, update_user, tags, description, module_id, path, latest, ref_id, file_version) VALUES ('wx_file_id_1', 'formItem', 'ts', 2502, 1698058347559, 1698058347559, '100001100001', 'MINIO', 'admin', 'admin', NULL, NULL, 'root', '100001100001/1127016598347779', b'1', '1127016598347779', '1127016598347779');

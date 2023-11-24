INSERT INTO file_metadata(id, name, type, size, create_time, update_time, project_id, storage, create_user, update_user, tags, description, module_id, path, latest, ref_id, file_version) VALUES ('relate_file_meta_id_1', 'formItem', 'ts', 2502, 1698058347559, 1698058347559, '100001100001', 'MINIO', 'admin', 'admin', NULL, NULL, 'root', '100001100001/1127016598347779', b'1', '1127016598347779', '1127016598347779');
INSERT INTO file_metadata(id, name, type, size, create_time, update_time, project_id, storage, create_user, update_user, tags, description, module_id, path, latest, ref_id, file_version) VALUES ('relate_file_meta_id_2', 'formItem', 'ts', 2502, 1698058347559, 1698058347559, '100001100001', 'MINIO', 'admin', 'admin', NULL, NULL, 'root', '100001100001/1127016598347779', b'1', '1127016598347779', '1127016598347779');

INSERT INTO file_association(id, source_type, source_id, file_id, file_ref_id, file_version, create_time, update_user, update_time, create_user) VALUES ('file_association_1', 'FUNCTIONAL_CASE', 'TEST_FUNCTIONAL_CASE_ID', 'relate_file_meta_id_1', '1', '1', 1698983271536, 'admin', 1698983271536, 'admin');


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID', 1, 'TEST_MODULE_ID', '100001100001', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID_1', 2, 'TEST_MODULE_ID', '100001100001', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID_2', 3, 'TEST_MODULE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v2.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID_3', 3, 'TEST_MODULE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID_4', 4, 'TEST_MODULE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID_5', 5, 'TEST_MODULE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'TEST_REF_ID_1', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID_6', 6, 'TEST_MODULE_ID', '100001100001', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'TEST_REF_ID_1', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_CASE_ID_7', 7, 'TEST_MODULE_ID', '100001100001', '100001', 'copy_long_name_11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'TEST_REF_ID_2', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID', 'STEP', '1111', NULL, NULL, 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID_1', 'STEP', '1111', NULL, NULL, '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID_2', 'STEP', '2222', NULL, NULL, '2222');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID_3', 'STEP', '3333', NULL, NULL, '3333');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID_4', 'STEP', '4444', NULL, NULL, '4444');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID_5', 'STEP', '5555', NULL, NULL, '5555');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID_6', 'STEP', '6666', NULL, NULL, '6666');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_CASE_ID_7', 'STEP', '7777', NULL, NULL, '7777');


INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_CASE_ID', '100548878725546079', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_CASE_ID', 'TEST_FIELD_ID', '["222","333"]');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_CASE_ID', 'TEST_FIELD_ID_1', '["222","333"]');


INSERT INTO functional_case_attachment(id, case_id, file_id, file_name, size, local, create_user, create_time) VALUES ('TEST_CASE_ATTACHMENT_ID', 'TEST_FUNCTIONAL_CASE_ID', '100548878725546079', '测试', 1, b'1', 'admin', 1698058347559);
INSERT INTO functional_case_attachment(id, case_id, file_id, file_name, size, local, create_user, create_time) VALUES ('TEST_CASE_ATTACHMENT_ID_1', 'TEST_FUNCTIONAL_CASE_ID', 'relate_file_meta_id_1', '测试1', 1, b'0', 'admin', 1698058347559);
INSERT INTO functional_case_attachment(id, case_id, file_id, file_name, size, local, create_user, create_time) VALUES ('TEST_CASE_ATTACHMENT_ID_2', 'TEST_FUNCTIONAL_CASE_ID', 'delete_file_meta_id_1', '测试删除', 1, b'1', 'admin', 1698058347559);

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('TEST_MODULE_ID', '100001100001', '测试所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');

INSERT INTO custom_field(id, name, scene, type, remark, internal, scope_type, create_time, update_time, create_user, ref_id, enable_option_key, scope_id) VALUES ('custom_field_id_1', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', b'1', 'ORGANIZATION', 1698983187000, 1698983187000, 'admin', NULL, b'0', '100001');

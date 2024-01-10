INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('select-case-tmp', 100, 'select-case-module', 'select-case-pro', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('select-case', 100, 'select-case-module', 'select-case-pro', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, create_user, update_time, update_user)
VALUES ('select-case-module', 'select-case-pro', 'test_module_name', 'NONE', '1', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin');
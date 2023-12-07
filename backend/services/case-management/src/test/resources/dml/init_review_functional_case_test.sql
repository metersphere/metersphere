INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_1', 100, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_2', 101, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_1', 'STEP', '1111', '', '', 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_2', 'STEP', '1111', '', '', '1111');


INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_1', '100548878725546079', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_1', 'TEST_FIELD_ID', '["222","333"]');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_2', 'TEST_FIELD_ID_1', '["222","333"]');



INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('wx_review_id_1',10001,'wx1', 'wx_module_1', 'wx_test_project', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_2',10002,'wx2', 'wx_module_2', 'wx_test_project', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin');

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time)
VALUES ('wx_test_1', 'wx_review_id_1', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559),
       ('wx_test_2', 'wx_review_id_2', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559);


INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('wx_module_1', 'wx_test_project', '测试所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');

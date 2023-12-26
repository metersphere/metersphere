INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_1', 100, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_2', 101, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyq_case_id_3', 102, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyq_case_id_4', 102, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_4', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_3', 104, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_4', 104, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_1', 'STEP', '1111', '', '', 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_2', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_case_id_3', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_case_id_4', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_3', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_4', 'STEP', '1111', '', '', '1111');



INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_1', '100548878725546079', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_1', 'TEST_FIELD_ID', '["222","333"]');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_2', 'TEST_FIELD_ID_1', '["222","333"]');



INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('wx_review_id_1',10001,'wx1', 'wx_module_1', 'wx_test_project', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_2',10002,'wx2', 'wx_module_2', 'wx_test_project', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_3',10003,'wx3', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_4',10004,'wx3', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin');

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('wx_test_1', 'wx_review_id_1', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 1000),
       ('gyq_test_3', 'wx_review_id_1', 'gyq_case_id_3', 'PASS', 1698058347559,'admin',1698058347559, 1500),
       ('gyq_test_4', 'wx_review_id_1', 'gyq_case_id_4', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('wx_test_2', 'wx_review_id_2', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_3', 'wx_review_id_2', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_4', 'wx_review_id_2', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_5', 'wx_review_id_3', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_6', 'wx_review_id_3', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_7', 'wx_review_id_3', 'wx_case_id_3', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_8', 'wx_review_id_4', 'wx_case_id_4', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_9', 'wx_review_id_4', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_10', 'wx_review_id_4', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559, 3000);

INSERT INTO case_review_functional_case_user(case_id, review_id, user_id)
VALUES ('wx_case_id_1', 'wx_review_id_1', 'admin'),
       ('wx_case_id_1', 'wx_review_id_1', 'gyq'),
       ('gyq_case_id_3', 'wx_review_id_1', 'gyq2'),
       ('gyq_case_id_4', 'wx_review_id_1', 'gyq'),
       ('wx_case_id_3', 'wx_review_id_3', 'admin'),
       ('wx_case_id_4', 'wx_review_id_4', 'admin'),
       ('wx_case_id_1', 'wx_review_id_4', '123'),
       ('wx_case_id_1', 'wx_review_id_4', 'admin'),
       ('wx_case_id_2', 'wx_review_id_4', '123'),
       ('wx_case_id_2', 'wx_review_id_4', 'admin');



INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('wx_module_1', 'wx_test_project', '测试所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');


INSERT INTO case_review_history(id, review_id, case_id, content, status, deleted, notifier, create_user, create_time)
VALUES ('wx_history', 'wx_review_id_3', 'wx_case_id_1', NULL, 'PASS', b'0', NULL, 'admin', 1669174143999),
       ('wx_histor_1', 'wx_review_id_3', 'wx_case_id_1', NULL, 'PASS', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_2', 'wx_review_id_3', 'wx_case_id_3', NULL, 'PASS', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_3', 'wx_review_id_4', 'wx_case_id_4', NULL, 'PASS', b'0', NULL, 'A', 1669174143999),
       ('wx_history_4', 'wx_review_id_4', 'wx_case_id_1', NULL, 'PASS', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_5', 'wx_review_id_4', 'wx_case_id_2', NULL, 'UN_PASS', b'0', NULL, 'admin', 1669174143999);


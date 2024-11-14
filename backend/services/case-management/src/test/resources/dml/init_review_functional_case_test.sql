INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_1', 100, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_2', 101, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyq_review_case_id_3', 102, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyq_case_id_4', 106, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyq_case_id_5', 102, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_4', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_case_id_7', 102, 'TEST_MODULE_ID_COUNT_three', 'wx_test_project', '100001', '测试模块', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_case_id_8', 102, 'TEST_MODULE_ID_COUNT_three', 'wx_test_project', '100001', '测试模块2', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_8', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_case_id_9', 104, 'TEST_MODULE_ID_COUNT_four', 'wx_test_project_review_two', '100001', '测试模块2-1', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_9', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_case_id_a', 105, 'TEST_MODULE_ID_COUNT_five', 'wx_test_project_review_two', '100001', '测试模块2-2', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_a', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_case_id_b', 105, 'TEST_MODULE_ID_COUNT_six', 'wx_test_project_review_one', '100001', '测试模块2-2', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_a', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
      ('gyq_case_id_c', 105, 'TEST_MODULE_ID_COUNT_seven', 'wx_test_project_review_one', '100001', '测试模块2-2', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_a', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_case_id_d', 105, 'root', 'wx_test_project_review_one', '100001', '测试默认模块', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'gyq_case_id_d', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('wx_test_id_7', 105, 'root', 'wx_test_project', '100001', '测试默认模块', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'wx_test_id_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('wx_test_id_8', 105, 'root', 'wx_test_project', '100001', '测试默认模块', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'wx_test_id_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('wx_test_id_9', 105, 'root', 'wx_test_project', '100001', '测试默认模块', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'wx_test_id_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_3', 104, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_case_id_4', 104, 'TEST_MODULE_ID', 'wx_test_project', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_1', 'STEP', '1111', '', '', 'TEST');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_2', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_review_case_id_3', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_case_id_4', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_case_id_5', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_3', 'STEP', '1111', '', '', '1111');
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('wx_case_id_4', 'STEP', '1111', '', '', '1111');



INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_1', '100548878725546079', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_1', 'TEST_FIELD_ID', '["222","333"]');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('wx_case_id_2', 'TEST_FIELD_ID_1', '["222","333"]');



INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('wx_review_id_1',10001,'wx1', 'wx_module_1', 'wx_test_project', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_2',10002,'wx2', 'wx_module_2', 'wx_test_project', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_3',10003,'wx3', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_4',10004,'wx3', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_5',10004,'gyq5', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_6',10005,'wx6', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_8',10007,'wx8', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('wx_review_id_9',10007,'wx8', 'wx_module_3', 'wx_test_project', 'COMPLETED', 'MULTIPLE', 003, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin');

    ;

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('wx_test_1', 'wx_review_id_1', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 1000),
       ('gyq_test_3', 'wx_review_id_1', 'gyq_review_case_id_3', 'PASS', 1698058347559,'admin',1698058347559, 1500),
       ('gyq_test_4', 'wx_review_id_1', 'gyq_case_id_4', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('gyq_test_5', 'wx_review_id_1', 'gyq_case_id_5', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('gyq_test_7', 'wx_review_id_1', 'gyq_case_id_7', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('gyq_test_8', 'wx_review_id_1', 'gyq_case_id_8', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('gyq_test_9', 'wx_review_id_2', 'gyq_case_id_9', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('gyq_test_a', 'wx_review_id_2', 'gyq_case_id_a', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('gyq_test_b', 'wx_review_id_2', 'gyq_case_id_b', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('gyq_test_c', 'wx_review_id_2', 'gyq_case_id_c', 'PASS', 1698058347559,'admin',1698058347559, 2000),
       ('wx_test_2', 'wx_review_id_2', 'wx_case_id_3', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_3', 'wx_review_id_2', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_4', 'wx_review_id_2', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_5', 'wx_review_id_3', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_6', 'wx_review_id_3', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_7', 'wx_review_id_3', 'wx_case_id_3', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_8', 'wx_review_id_4', 'wx_case_id_4', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_9', 'wx_review_id_4', 'wx_case_id_1', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_10', 'wx_review_id_4', 'wx_case_id_2', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_11', 'wx_review_id_5', 'gyq_case_id_d', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_12', 'wx_review_id_6', 'wx_test_id_7', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_13', 'wx_review_id_8', 'wx_test_id_8', 'PASS', 1698058347559,'admin',1698058347559, 3000),
       ('wx_test_14', 'wx_review_id_9', 'wx_test_id_9', 'PASS', 1698058347559,'admin',1698058347559, 3000);

INSERT INTO case_review_functional_case_user(case_id, review_id, user_id)
VALUES ('wx_case_id_1', 'wx_review_id_1', 'admin'),
       ('wx_case_id_1', 'wx_review_id_1', 'gyq_case_review'),
       ('wx_test_1', 'wx_review_id_1', 'admin'),
       ('gyq_review_case_id_3', 'wx_review_id_1', 'admin'),
       ('gyq_case_id_4', 'wx_review_id_1', 'admin'),
       ('gyq_case_id_5', 'wx_review_id_1', 'gyq_case_review'),
       ('gyq_case_id_5', 'wx_review_id_1', 'GGG'),
       ('gyq_case_id_5', 'wx_review_id_1', 'multiple_review_admin'),
       ('wx_case_id_3', 'wx_review_id_3', 'admin'),
       ('wx_case_id_4', 'wx_review_id_4', 'admin'),
       ('wx_case_id_1', 'wx_review_id_4', '123'),
       ('wx_case_id_1', 'wx_review_id_4', 'admin'),
       ('wx_case_id_2', 'wx_review_id_4', '123'),
       ('wx_case_id_2', 'wx_review_id_4', 'admin'),
       ('gyq_case_id_d', 'wx_review_id_5', 'admin'),
       ('wx_test_id_8', 'wx_review_id_8', 'admin'),
       ('wx_test_id_9', 'wx_review_id_9', 'admin');



INSERT INTO case_review_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('wx_module_1', 'wx_test_project', '测试所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user)
VALUES ('TEST_MODULE_ID', 'wx_test_project', '测试所属模块', 'NONE', 1568, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MODULE_ID_COUNT_ID', 'wx_test_project', '测试所属模块子集', 'TEST_MODULE_ID', 64, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MODULE_ID_COUNT_three', 'wx_test_project', '测试所属模块子集2', 'TEST_MODULE_ID_COUNT_ID', 64, 1669174143999, 1669174143999, 'admin', 'admin'),
       ('TEST_MODULE_ID_COUNT_six', 'wx_test_project_review_one', '测试多项目模块3', 'NONE', 8, 1669174143999, 1669174143999, 'admin', 'admin'),
       ('TEST_MODULE_ID_COUNT_seven', 'wx_test_project_review_one', '测试多项目模块4', 'TEST_MODULE_ID_COUNT_six', 16, 1669174143999, 1669174143999, 'admin', 'admin'),
       ('TEST_MODULE_ID_COUNT_four', 'wx_test_project_review_two', '测试多项目模块', 'NONE', 56, 1669174143999, 1669174143999, 'admin', 'admin'),
('TEST_MODULE_ID_COUNT_five', 'wx_test_project_review_two', '测试多项目模块2', 'TEST_MODULE_ID_COUNT_four', 48, 1669174143999, 1669174143999, 'admin', 'admin');

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('wx_test_project_review_one', null, 'organization-associate-case-test', '评审跨项目关联用例', '系统默认创建的项目',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
    ('wx_test_project_review_two', null, 'organization-associate-case-test', '评审跨项目关联用例2', '系统默认创建的项目',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('wx_test_project', null, 'organization-associate-case-test', '评审跨项目关联用例3', '系统默认创建的项目',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);


INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('gyq_case_review', 'gyq_case_review', 'gyq_case_review_case@fit2cloud.com', MD5('metersphere'),UNIX_TIMESTAMP() * 1000,UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
       ('GGG', 'GGG', 'GGG_case_review_case@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
       ('multiple_review_admin', 'multiple_review_admin', 'multiple_review_admin@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
        ('123', '123', '123@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false);


INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUE (UUID(), 'multiple_review_admin', 'admin', 'system',
        'system', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO case_review_history(id, review_id, case_id, content, status, deleted, abandoned, notifier, create_user, create_time)
VALUES ('wx_history', 'wx_review_id_3', 'wx_case_id_1', NULL, 'PASS', b'0', b'0', NULL, 'admin', 1669174143999),
       ('wx_histor_1', 'wx_review_id_3', 'wx_case_id_1', NULL, 'PASS', b'0', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_2', 'wx_review_id_3', 'wx_case_id_3', NULL, 'PASS', b'0', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_3', 'wx_review_id_4', 'wx_case_id_4', NULL, 'PASS', b'0', b'0', NULL, 'A', 1669174143999),
       ('wx_history_4', 'wx_review_id_4', 'wx_case_id_1', NULL, 'PASS', b'0', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_5', 'wx_review_id_4', 'wx_case_id_2', NULL, 'UN_PASS', b'0', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_6', 'wx_review_id_1', 'gyq_case_id_5', NULL, 'PASS', b'0', b'0', NULL, 'gyq_case_review', 1669174143999),
       ('wx_history_7', 'wx_review_id_1', 'gyq_case_id_5', NULL, 'UN_PASS', b'0', b'0', NULL, 'GGG', 1669174143999),
       ('wx_history_8', 'wx_review_id_1', 'wx_case_id_1', NULL, 'UN_PASS', b'0', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_9', 'wx_review_id_5', 'gyq_case_id_d', NULL, 'RE_REVIEWED', b'0', b'0', NULL, 'GGG', 1669174143999),
       ('wx_history_19', 'wx_review_id_8', 'wx_test_id_8', NULL, 'RE_REVIEWED', b'0', b'0', NULL, 'admin', 1669174143999),
       ('wx_history_20', 'wx_review_id_9', 'wx_test_id_9', NULL, 'UN_PASS', b'0', b'0', NULL, 'admin', 1669174143999);

INSERT INTO case_review_user(review_id, user_id)
VALUES ('wx_review_id_4', 'admin')
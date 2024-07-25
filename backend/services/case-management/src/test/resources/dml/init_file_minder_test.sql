INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-case-minder-test', null, '100001', '用例脑图项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-case-minder-test-xx', null, '100001', '用例脑图项目权限', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'admin', 'project_admin', 'project-case-minder-test', '100001', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', 1, 'TEST_MINDER_MODULE_ID_GYQ', 'project-case-minder-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_2', 2, 'TEST_MINDER_MODULE_ID_GYQ', 'project-case-minder-test', '100001', '测试多版本', 'UN_REVIEWED', '["测试标签_1"]', 'TEXT', 5000, 'v1.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_2', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_3', 3, 'TEST_MINDER_MODULE_ID_GYQ2', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 25000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_3', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_4', 4, 'TEST_MINDER_MODULE_ID_GYQ3', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 40000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_4', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_5', 5, 'TEST_MINDER_MODULE_ID_GYQ4', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 45000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_5', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
    ('TEST_FUNCTIONAL_MINDER_CASE_ID_6', 6, 'TEST_MINDER_MODULE_ID_GYQ5', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_8', 3, 'TEST_MINDER_MODULE_ID_GYQ2', 'project-case-minder-test', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'STEP', 25000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_8', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),

       ('TEST_FUNCTIONAL_MINDER_CASE_ID_7', 7, 'TEST_MINDER_MODULE_ID_GYQ6', 'project-case-minder-test-xx', '100001', 'copy_测试多版本', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_9', 8, 'TEST_MINDER_MODULE_ID_GYQ6', 'project-case-minder-test', '100001', 'copy_用来删除', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);



INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_3', NULL, NULL, NULL, NULL, NULL);
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_5', NULL, NULL, NULL, NULL, NULL);
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_6', NULL, NULL, NULL, NULL, NULL);



INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'custom_field_minder_gyq_id_3', 'P0');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_2', 'custom_field_minder_gyq_id_3', 'P3');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_5', 'custom_field_minder_gyq_id_3', 'P3');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'custom_field_minder_gyq_id_3', 'P2');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'custom_field_minder_gyq_id_4', 'a');




INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user)
VALUES
    ('TEST_MINDER_MODULE_ID_GYQ', 'project-case-minder-test', '测试所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ2', 'project-case-minder-test', '测试所属模块2', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ3', 'project-case-minder-test', '测试所属模块3', 'TEST_MINDER_MODULE_ID_GYQ2', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ4', 'project-case-minder-test', '测试所属模块4', 'TEST_MINDER_MODULE_ID_GYQ3', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ5', 'project-case-minder-test', '测试所属模块5', 'TEST_MINDER_MODULE_ID_GYQ4', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ7', 'project-case-minder-test', '测试所属模块7', 'TEST_MINDER_MODULE_ID_GYQ', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ8', 'project-case-minder-test', '测试所属模块8', 'TEST_MINDER_MODULE_ID_GYQ', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ6', 'project-case-minder-test-xx', '测试所属模块1', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ9', 'project-case-minder-test', '用来删除', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
    ('TEST_MINDER_MODULE_ID_GYQ_A', 'project-case-minder-test', '模块转用例', 'TEST_MINDER_MODULE_ID_GYQ8', 0, 1669174143999, 1669174143999, 'admin', 'admin');



INSERT INTO test_plan(id, num, project_id, group_id, module_id, name, status, type, tags, create_time, create_user, update_time, update_user, planned_start_time, planned_end_time, actual_start_time, actual_end_time, description)
    VALUE ('TEST_MINDER_PLAN_ID_1', 1000, 'project-case-minder-test', 'none', 'TEST_MINDER_PLAN_MODULE', '脑图测试计划', 'PREPARED', 'TEST_PLAN', null, 1669174143999, 'admin', 1669174143999, 'admin', null, null, null,null,null);

INSERT INTO test_plan_functional_case(id, test_plan_id, functional_case_id, create_time, create_user, execute_user, last_exec_time, last_exec_result, pos, test_plan_collection_id)
    VALUES ('test_plan_functional_case_minder_id1', 'TEST_MINDER_PLAN_ID_1', 'TEST_FUNCTIONAL_MINDER_CASE_ID_5', 1669174143999, 'admin', 'admin', 1669174143999, 'SUCCESS', 5000, '123'),
         ('test_plan_functional_case_minder_id2', 'TEST_MINDER_PLAN_ID_1', 'TEST_FUNCTIONAL_MINDER_CASE_ID_5', 1669174143999, 'admin', 'admin', 1669174143999, 'SUCCESS', 10000, '123');

INSERT INTO custom_field (id, name, scene, type, remark, internal, scope_type, create_time, update_time, create_user, scope_id) VALUES
    ('custom_field_minder_gyq_id_2', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', true, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001'),
    ('custom_field_minder_gyq_id_3', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', true, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-case-minder-test'),
    ('custom_field_minder_gyq_id_4', 'fff', 'FUNCTIONAL', 'SELECT', '', true, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-case-minder-test');



INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
    VALUES ('TEST_MINDER_REVIEW_ID_GYQ', 100, 'TEST_MINDER_REVIEW_GYQ', 'TEST_MINDER_REVIEW_MODULE_GYQ', 'project-case-minder-test', 'UNDERWAY', 'MULTIPLE', 100,UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 6, 50.00, null, null, UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin'),
           ('TEST_MINDER_REVIEW_ID_GYQ2', 200, 'TEST_MINDER_REVIEW_GYQ2', 'TEST_MINDER_REVIEW_MODULE_GYQ', 'project-case-minder-test', 'UNDERWAY', 'SINGLE', 200,UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 6, 50.00, null, null, UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('case_review_functional_case_minder_id1', 'TEST_MINDER_REVIEW_ID_GYQ', 'TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'PASS', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id2', 'TEST_MINDER_REVIEW_ID_GYQ', 'TEST_FUNCTIONAL_MINDER_CASE_ID_2', 'PASS', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id3', 'TEST_MINDER_REVIEW_ID_GYQ', 'TEST_FUNCTIONAL_MINDER_CASE_ID_3', 'PASS', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id4', 'TEST_MINDER_REVIEW_ID_GYQ', 'TEST_FUNCTIONAL_MINDER_CASE_ID_4', 'UNDER_REVIEWED', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id5', 'TEST_MINDER_REVIEW_ID_GYQ', 'TEST_FUNCTIONAL_MINDER_CASE_ID_5', 'UNDER_REVIEWED', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id6', 'TEST_MINDER_REVIEW_ID_GYQ', 'TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'UNDER_REVIEWED', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id7', 'TEST_MINDER_REVIEW_ID_GYQ2', 'TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'PASS', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id8', 'TEST_MINDER_REVIEW_ID_GYQ2', 'TEST_FUNCTIONAL_MINDER_CASE_ID_2', 'PASS', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_id9', 'TEST_MINDER_REVIEW_ID_GYQ2', 'TEST_FUNCTIONAL_MINDER_CASE_ID_3', 'PASS', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_ida', 'TEST_MINDER_REVIEW_ID_GYQ2', 'TEST_FUNCTIONAL_MINDER_CASE_ID_4', 'UNDER_REVIEWED', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_idb', 'TEST_MINDER_REVIEW_ID_GYQ2', 'TEST_FUNCTIONAL_MINDER_CASE_ID_5', 'UNDER_REVIEWED', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 ),
       ('case_review_functional_case_minder_idc', 'TEST_MINDER_REVIEW_ID_GYQ2', 'TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'UNDER_REVIEWED', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 100 );


INSERT INTO case_review_functional_case_user(case_id, review_id, user_id)
VALUES ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'TEST_MINDER_REVIEW_ID_GYQ', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_2', 'TEST_MINDER_REVIEW_ID_GYQ', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_3', 'TEST_MINDER_REVIEW_ID_GYQ', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_4', 'TEST_MINDER_REVIEW_ID_GYQ', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_5', 'TEST_MINDER_REVIEW_ID_GYQ', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'TEST_MINDER_REVIEW_ID_GYQ', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_1', 'TEST_MINDER_REVIEW_ID_GYQ2', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_2', 'TEST_MINDER_REVIEW_ID_GYQ2', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_3', 'TEST_MINDER_REVIEW_ID_GYQ2', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_4', 'TEST_MINDER_REVIEW_ID_GYQ2', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_5', 'TEST_MINDER_REVIEW_ID_GYQ2', 'admin'),
       ('TEST_FUNCTIONAL_MINDER_CASE_ID_6', 'TEST_MINDER_REVIEW_ID_GYQ2', 'admin');

INSERT INTO mind_additional_node(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user)
VALUES ('additional1', 'project-case-minder-test', 'additional1', 'TEST_MINDER_MODULE_ID_GYQ', 0, UNIX_TIMESTAMP() * 1000,UNIX_TIMESTAMP() * 1000, 'admin','admin'),
       ('additional2', 'project-case-minder-test', 'additional2', 'additional1', 500, UNIX_TIMESTAMP() * 1000,UNIX_TIMESTAMP() * 1000, 'admin','admin');

INSERT INTO template(id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id)
VALUES ('100001', 'functional_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'project-case-minder-test', 0, 'FUNCTIONAL', '100001');
INSERT INTO template_custom_field(id, field_id, template_id, required, pos, api_field_id, default_value) VALUES ('template_custom_field_minder_gyq_id', 'custom_field_minder_gyq_id_3', '100001', b'1', 0, NULL, NULL);
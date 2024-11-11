
INSERT INTO user(id, name, email, password, enable, create_time, update_time, language, last_organization_id, phone,
                 source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('default-dashboard-member-user-gyq', 'default-project-member-user1', 'project-member-gyqDashboard@metersphere.io',
        MD5('metersphere'),
        true, UNIX_TIMESTAMP() * 1000,
        UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false);




INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-dashboard-member-user-gyq', 'org_member', 'organization-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-dashboard-member-user-gyq', 'project_admin', 'project-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_permission(id, role_id, permission_id)
VALUES ('user_role_gyq_permission1', 'project_admin', 'FUNCTIONAL_CASE:READ+COMMENT');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID_g', 8, 'Trash_TEST_MOUDLE_ID_1', '100001100001', '100001', 'copy_测试多版本g', 'UN_REVIEWED', NULL, 'STEP', 0, 'v3.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_f', 'UN_EXECUTED',false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID_GYQ', 10, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', '回收站测信', 'UNDER_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'Trash_TEST_FUNCTIONAL_CASE_ID_GYQ', 'UN_EXECUTED', false, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID', 20, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', '回收站测信', 'PASS', NULL, 'STEP', 0, 'v1.0.0', 'unTrash_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED', false, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID5', 30, 'Trash_TEST_MOUDLE_ID', '100001100001', '100001', '回收站测信', 'RE_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'unTrash_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED', false, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('dashboardGyq_review_id', 10001, '用例评审1', 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test', 'COMPLETE' , 'SINGLE', 0, 1698058347559, 1698058347559, 1, 100.00, null, null, 1698058347559, 'admin', 1698058347559, 'admin');

INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('dashboardGyq_review_id1', 10001, '用例评审2', 'Trash_TEST_MOUDLE_ID_1', 'project-case-trash-test', 'COMPLETE' , 'SINGLE', 0, 1698058347559, 1698058347559, 1, 100.00, null, null, 1698058347559, 'admin', 1698058347559, 'admin');

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('dashboardGyq_Review_Case_Id','dashboardGyq_review_id', 'dashboard_TEST_FUNCTIONAL_CASE_ID_GYQ', 'UNDER_REVIEWED', 1698058347559, 'admin',1698058347559, 0);

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('dashboardGyq_Review_Case_Id2','dashboardGyq_review_id1', 'dashboard_TEST_FUNCTIONAL_CASE_ID5', 'RE_REVIEWED', 1698058347559, 'admin',1698058347559, 0);

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('dashboardGyq_Review_Case_Id3','dashboardGyq_review_id', 'dashboard_TEST_FUNCTIONAL_CASE_ID_g', 'UN_REVIEWED', 1698058347559, 'admin',1698058347559, 0);


INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time, pos)
VALUES ('dashboardGyq_Review_Case_Id4','dashboardGyq_review_id', 'dashboard_TEST_FUNCTIONAL_CASE_ID', 'PASS', 1698058347559, 'admin',1698058347559, 0);

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID_g', 'gyq_custom_id1', 'P0');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID', 'gyq_custom_id1', 'P2');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID5', 'gyq_custom_id1', 'P3');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('dashboard_TEST_FUNCTIONAL_CASE_ID_GYQ', 'gyq_custom_id1', 'P1');

INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id1', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_trash_id');




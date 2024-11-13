
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

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('dashboard_bug1', 100001, 'oasis', 'admin', 'admin', 'admin', 1697971947000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('dashboard_bug2', 100002, 'oasis2', 'PROJECT', 'PROJECT', 'admin', 1697971947000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('dashboard_bug3', 100003, 'oasis3', 'admin', 'admin', 'admin', 1697971947000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('dashboard_api_definition_id_1', 'gyq_associate_api_definition_id_1', 'HTTP', 'POST','api/test','DONE', 1000001, null, 1, '100001100001' , 'gyq_associate_test_module', true, 'v1.10','gyq_associate_api_definition_id_1', null, 1697971947000,'admin', 1697971947000,'admin', null,null,false);

INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('dashboard_api_definition_id_2', 'gyq_associate_api_definition_id_2', 'TCP', 'POST','api/test','PROCESSING', 1000001, null, 1, '100001100001' , 'gyq_associate_test_module', true, 'v1.10','gyq_associate_api_definition_id_1', null, 1697971947000,'admin', 1697971947000,'admin', null,null,false);

INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('dashboard_api_definition_id_3', 'gyq_associate_api_definition_id_2', 'DUBBO', 'POST','api/test','DEPRECATED', 1000001, null, 1, '100001100001' , 'gyq_associate_test_module', true, 'v1.10','gyq_associate_api_definition_id_1', null, 1697971947000,'admin', 1697971947000,'admin', null,null,false);

INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('dashboard_api_definition_id_4', 'gyq_associate_api_definition_id_2', 'HTTP', 'POST','api/test','DEBUGGING', 1000001, null, 1, '100001100001' , 'gyq_associate_test_module', true, 'v1.10','gyq_associate_api_definition_id_1', null, 1697971947000,'admin', 1697971947000,'admin', null,null,false);

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted) VALUES
    ('dashboard_ac_1', 'oasis_ac', 'P0', 1001, null, 'Underway', 'PENDING', null, 100, '100001100001', 'dashboard_api_definition_id_1', 'oasis_ac_version_id', 'oasis_ac_env_id', 1697971947000, 'admin', 1698058347559, 'admin', null, null, false);

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted) VALUES
    ('dashboard_ac_2', 'oasis_ac', 'P0', 1001, null, 'Underway', 'SUCCESS', null, 100, '100001100001', 'dashboard_api_definition_id_1', 'oasis_ac_version_id', 'oasis_ac_env_id', 1697971947000, 'admin', 1698058347559, 'admin', null, null, false);

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted) VALUES
    ('dashboard_ac_3', 'oasis_ac', 'P0', 1001, null, 'Underway', 'FAKE_ERROR', null, 100, '100001100001', 'dashboard_api_definition_id_1', 'oasis_ac_version_id', 'oasis_ac_env_id', 1697971947000, 'admin', 1698058347559, 'admin', null, null, false);

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted) VALUES
    ('dashboard_ac_4', 'oasis_ac', 'P0', 1001, null, 'Underway', 'ERROR', null, 100, '100001100001', 'dashboard_api_definition_id_1', 'oasis_ac_version_id', 'oasis_ac_env_id', 1697971947000, 'admin', 1698058347559, 'admin', null, null, false);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time)
VALUES ('dashboard_scenario_one', 'api_scenario', 'p1', 'test-api-status',  'PENDING', null,1000001, 1,'v1.10', 'aspect_gyq_api_scenario_one','100001100001', 'test_module', null,null,'admin',  1697971947000,null,null,'admin', 1697971947000);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time)
VALUES ('dashboard_scenario_two', 'api_scenario', 'p1', 'test-api-status',  'SUCCESS', null,1000001, 1,'v1.10', 'aspect_gyq_api_scenario_one','100001100001', 'test_module', null,null,'admin',  1697971947000,null,null,'admin', 1697971947000);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time)
VALUES ('dashboard_scenario_three', 'api_scenario', 'p1', 'test-api-status',  'FAKE_ERROR', null,1000001, 1,'v1.10', 'aspect_gyq_api_scenario_one','100001100001', 'test_module', null,null,'admin',  1697971947000,null,null,'admin', 1697971947000);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time)
VALUES ('dashboard_scenario_four', 'api_scenario', 'p1', 'test-api-status',  'ERROR', null,1000001, 1,'v1.10', 'aspect_gyq_api_scenario_one','100001100001', 'test_module', null,null,'admin',  1697971947000,null,null,'admin', 1697971947000);


INSERT INTO api_scenario_step(id, scenario_id, name, sort, enable, resource_id, resource_num, step_type, project_id, parent_id, version_id, ref_type, origin_project_id, config)
    VALUE ('dashboard_act_1', 'dashboard_sc_1', 'dd', 1, true, 'dashboard_api_definition_id_1', '1000', 'API', '100001100001', null, 'oasis_ac_version_id', 'REF', '100001100001', null)
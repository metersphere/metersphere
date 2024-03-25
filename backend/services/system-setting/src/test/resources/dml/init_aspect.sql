INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('aspect_gyq_one', 1000001, 'test_guo', '100001100001', 'test_guo', 'gyq_test_one', 'UN_REVIEWED', null, 'text',
        10001, '111', 'aspect_gyq_one', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('aspect_gyq_two', 1000001, 'test_guo', '100001100001', 'test_guo', 'gyq_test_two', 'UN_REVIEWED', null, 'text',
        10001, '111', 'aspect_gyq_two', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('aspect-member-user-guo', 'aspect-member-user-guo', 'aspect-member-user-guo@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'aspect-member-user-guo', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'aspect-member-user-guo', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_permission(id, role_id, permission_id)
VALUES ('aspect_user_role_guo_permission1', 'project_admin', 'FUNCTIONAL_CASE_COMMENT:READ+ADD'),
       ('aspect_user_role_guo_permission2', 'project_admin', 'FUNCTIONAL_CASE:READ+ADD');

INSERT INTO custom_field(id, name, scene, type, remark, create_time, update_time, create_user, ref_id, scope_id, internal, enable_option_key, scope_type)
values ('aspect_test_one','aspect_test','FUNCTIONAL', 'INPUT','aspect_test',  UNIX_TIMESTAMP() * 1000,  UNIX_TIMESTAMP() * 1000, 'admin' , 'aspect_test_one', '100001100001',false, false, 'PROJECT');

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('aspect_gyq_one', 'aspect_test_one', 'hello');

INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('aspect_gyq_api_one', 'api_test','HTTP', 'POST','api/test','test-api-status', 1000001, null, 1, '100001100001' , 'test_module', true, 'v1.10','aspect_gyq_api_one', null, UNIX_TIMESTAMP() * 1000,'admin', UNIX_TIMESTAMP() * 1000,'admin', null,null,false);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time)
VALUES ('aspect_gyq_api_scenario_one', 'api_scenario', 'p1', 'test-api-status',  'PENDING', null,1000001, 1,'v1.10', 'aspect_gyq_api_scenario_one','100001100001', 'test_module', null,null,'admin',  UNIX_TIMESTAMP() * 1000,null,null,'admin', UNIX_TIMESTAMP() * 1000);

INSERT INTO test_plan(id, project_id, group_id, module_id, name, status, type, tags, create_time, create_user,
                      update_time, update_user, planned_start_time, planned_end_time, actual_start_time,
                      actual_end_time, description)
VALUES ('aspect_gyq_test_plan_one', '100001100001', 'none', 'root', 'test_plan', 'PREPARED', 'TEST_PLAN', null,
        UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 2000,
        UNIX_TIMESTAMP() * 3000, UNIX_TIMESTAMP() * 2000, UNIX_TIMESTAMP() * 3000, null);

INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, tags, description, create_time, create_user, update_time, update_user)
VALUES ('aspect_gyq_case_review_one','10001','case_review','module_id', '100001100001','PREPARED','SINGLE','0', null, null, null, null, UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin');



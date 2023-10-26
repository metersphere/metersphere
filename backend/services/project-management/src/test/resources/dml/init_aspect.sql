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

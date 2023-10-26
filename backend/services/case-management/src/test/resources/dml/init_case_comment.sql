INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('xiaomeinvGTest', 1000001, 'test_guo', '100001100001', 'test_guo', 'gyqTest', 'UN_REVIEWED', null, 'text',
        10001, '111', 'xiaomeinvGTest', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('xiaomeinvGTestOne', 1000001, 'test_guo', '100001100001', 'test_guo', 'gyqTest1', 'UN_REVIEWED', null, 'text',
        10001, '111', 'xiaomeinvGTestOne', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('default-project-member-user-guo', 'default-project-member-user1', 'project-member-guo1@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-1', 'default-project-member-user2', 'project-member-guo2@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-2', 'default-project-member-user3', 'project-member-guo3@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-3', 'default-project-member-user4', 'project-member-guo4@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-4', 'default-project-member-user5', 'project-member-guo5@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-del', 'default-project-member-userDel',
        'project-member-guo-del@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000,
        NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1),
       ('gyq', 'gyq', 'gyq@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000,
        NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-guo', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'default-project-member-user-guo-1', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-2', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-3', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-4', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-del', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'gyq', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-guo', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-1', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-2', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-3', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-4', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-del', 'project_admin', '100001100001', '100001',
        UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'gyq', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_permission(id, role_id, permission_id)
VALUES ('user_role_guo_permission1', 'project_admin', 'FUNCTIONAL_CASE_COMMENT:READ+ADD'),
       ('user_role_guo_permission2', 'project_admin', 'FUNCTIONAL_CASE_COMMENT:READ+DELETE'),
       ('user_role_guo_permission3', 'project_admin', 'FUNCTIONAL_CASE:READ+ADD');



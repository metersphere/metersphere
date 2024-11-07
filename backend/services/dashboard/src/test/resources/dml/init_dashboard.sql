
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




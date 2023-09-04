# 组织用户组数据准备
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-id-1', 'default-org-role-1', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-2');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-id-2', 'default-org-role-2', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-2');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-id-3', 'default-org-role-3', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-2');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-id-4', 'default-org-role-4', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-2');
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
    ('default-admin-user', 'default-Administrator-1', 'admin-default-user@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUE
    (uuid(), 'default-org-role-id-3', 'ORGANIZATION_USER_ROLE:READ');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'default-admin-user', 'default-org-role-id-3', 'default-organization-2', 'default-organization-2', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'default-admin-user', 'default-org-role-id-4', 'default-organization-2', 'default-organization-2', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'default-admin-user-x', 'default-org-role-id-4', 'default-organization-2', 'default-organization-2', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'default-admin-user-x', 'default-org-role-id-3', 'default-organization-4', 'default-organization-4', UNIX_TIMESTAMP() * 1000, 'admin');
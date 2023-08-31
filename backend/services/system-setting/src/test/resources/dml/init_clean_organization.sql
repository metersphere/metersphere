# 定时删除组织列表数据准备
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('default-organization-delete1', null, 'default-delete1', 'XXX-delete1', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('default-organization-delete2', null, 'default-delete2', 'XXX-delete2', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 1, 'admin', UNIX_TIMESTAMP() - 2592000000);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('default-organization-delete3', null, 'default-delete3', 'XXX-delete3', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 1, 'admin', UNIX_TIMESTAMP() - 2592000000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-delete', null, 'default-organization-delete2', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-org-role-delete-id', 'default-org-role-delete', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-organization-delete2');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'admin', 'default-org-role-delete-id', 'default-organization-delete2', 'default-organization-delete2', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUE
    (uuid(), 'default-org-role-delete-id', 'ORGANIZATION_USER_ROLE:READ');

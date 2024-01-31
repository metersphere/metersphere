# 组织列表数据准备
INSERT INTO organization(id,num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('default-organization-1',null, 'default-1', 'XXX-1', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('default-organization-2',null, 'default-2', 'XXX-2', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('default-organization-3',null, 'default-3', 'XXX-3', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('default-organization-4',null, 'default-4', 'XXX-4', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id,num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('default-organization-5',null, 'default-5', 'XXX-5', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time, deleted) VALUE
    ('default-organization-6',null, 'default-6', 'XXX-6', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, UNIX_TIMESTAMP() * 1000, true);
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
   ('default-admin', 'default-Administrator', 'admin-default@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'admin', 'org_admin', 'default-organization-2', 'default-organization-2', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'default-admin', 'org_admin', 'default-organization-3', 'default-organization-3', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project', null, 'default-organization-2', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
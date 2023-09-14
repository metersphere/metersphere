# 项目用户组数据准备
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time,
                     update_time) VALUE
    ('default-project-1', null, 'default-organization-1', '默认项目-1', '系统默认创建的项目-1', 'admin', 'admin',
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time,
                     update_time) VALUE
    ('default-project-2', null, 'default-organization-2', '默认项目-2', '系统默认创建的项目-2', 'admin', 'admin',
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time,
                     update_time) VALUE
    ('default-project-3', null, 'default-organization-2', '默认项目-2', '系统默认创建的项目-2', 'admin', 'admin',
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user) VALUE
    ('default-pro-admin-user', 'default-pro-admin-1', 'admin-default-pro-user@metersphere.io', MD5('metersphere'),
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id)
VALUES ('default-pro-role-id-1', 'default-pro-role-1', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000,
        UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-2'),
       ('default-pro-role-id-2', 'default-pro-role-2', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000,
        UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-2'),
       ('default-pro-role-id-3', 'default-pro-role-3', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000,
        UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-2'),
       ('default-pro-role-id-4', 'default-pro-role-4', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000,
        UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-2');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUE (uuid(), 'default-org-role-id-3', 'PROJECT_USER_GROUP:READ');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-pro-admin-user', 'default-pro-role-id-3', 'default-project-2', 'default-project-2',
        UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'default-pro-admin-user', 'default-pro-role-id-4', 'default-project-2', 'default-project-2',
        UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'default-admin-user-x', 'default-pro-role-id-4', 'default-project-2', 'default-project-2',
        UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'default-admin-user-x', 'default-pro-role-id-3', 'default-organization-4', 'default-project-4',
        UNIX_TIMESTAMP() * 1000, 'admin');
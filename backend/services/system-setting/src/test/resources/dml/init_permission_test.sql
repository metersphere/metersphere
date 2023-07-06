-- 初始化一个没有任何权限的用户
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('permission1', 'permission_test', 'permission@fit2cloud.com', MD5('permission@fit2cloud.com'), UNIX_TIMESTAMP() * 1000,
        UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false);

-- 初始化一个没有任何权限的用户组
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id)
VALUES ('permission_member', '权限测试账号', '权限测试账号', 1, 'SYSTEM', 1620674220005, 1620674220000, 'admin',
        'GLOBAL');

-- 初始化用户和组的关系
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user)
VALUES (uuid(), 'permission1', 'org_admin', 'SYSTEM', 1684747668375, 'admin');

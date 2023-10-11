-- 初始化一个系统管理员
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user, deleted)
VALUES ('test.login', 'test.login', 'test.login@163.com', MD5('test.login@163.com'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false);

-- 管理员
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user, deleted)
VALUES ('test.login1', 'test.login1', 'test.login1@163.com', MD5('test.login1@163.com'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, '100001', '', 'LOCAL', 'test-login-project', 'admin', 'admin', false);

replace
    INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES ('test-login-user1', 'test.login1', 'admin', 'system','system', UNIX_TIMESTAMP() * 1000, 'admin');

replace INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user,
                          delete_time) VALUE
    ('test-login-organizationId', null, 'test-login-organizationId', 'test-login-organizationId', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin',
     null, null);
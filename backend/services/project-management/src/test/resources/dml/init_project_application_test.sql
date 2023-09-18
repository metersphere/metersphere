-- 模拟数据
INSERT INTO `service_integration`(`id`, `plugin_id`, `enable`, `configuration`, `organization_id`) VALUES ('1', '952262969139212', b'1', '1111', '100001');


-- 模拟用户
replace INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user) VALUE
    ('wx-test', 'wx-test-1', 'wx-test-user@metersphere.io', MD5('metersphere'),
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');

replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES (UUID_SHORT(), 'wx-test', 'project_admin', '100001100001', '100001', 1684747668375, 'admin');




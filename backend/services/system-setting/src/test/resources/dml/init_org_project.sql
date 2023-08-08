# 插入测试数据
replace into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user)
VALUES ('admin1', 'test1', 'admin1@metersphere.io', MD5('admin1@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', 'projectId1', 'admin', 'admin');
replace into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user)
VALUES ('admin1', 'test1', 'admin1@metersphere.io', MD5('admin1@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', 'projectId1', 'admin', 'admin');
replace into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user)
VALUES ('admin2', 'test2', 'admin2@metersphere.io', MD5('admin2@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
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

replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time,module_setting) VALUES ('projectId', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000, '["apiTest","uiTest"]');
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('projectId1', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目1', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('projectId2', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目2', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting) VALUES ('projectId3', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目3', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["apiTest","uiTest"]');
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time,module_setting) VALUES ('projectId4', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目4', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["apiTest","uiTest","loadTest"]');
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time,module_setting) VALUES ('projectId5', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目5', '系统默认创建的项目', 'test', 'test', unix_timestamp() * 1000, unix_timestamp() * 1000,'["apiTest","uiTest"]');

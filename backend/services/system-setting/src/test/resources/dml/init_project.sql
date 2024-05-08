# 插入测试数据
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time,
                      module_setting)
VALUES ('projectId', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目', '系统默认创建的项目',
        'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000, '["apiTest","uiTest"]');

replace INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user,
                          delete_time) VALUE
    ('organizationId', null, 'test-org', 'project', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin',
     null, null);

replace into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                  last_project_id, create_user, update_user)
VALUES ('admin1', 'test1', 'admin1@metersphere.io', MD5('admin1@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', 'projectId1', 'admin', 'admin');
replace
    into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
              last_project_id, create_user, update_user)
VALUES ('admin2', 'test2', 'admin2@metersphere.io', MD5('admin2@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
replace into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                  last_project_id, create_user, update_user)
VALUES ('test', 'test', 'admin3@metersphere.io', MD5('admin2@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');

replace
    INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES ('c3bb9b4f-46d8-4952-9681-8889974487w', 'admin1', 'project_admin', 'projectId1',
        (SELECT id FROM organization WHERE name LIKE '默认组织'), '1684747668375', '1684747668375');
replace
    INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES ('c3bb9b4f-46d8-4952-9681-8889974487q', 'admin2', 'project_admin', 'projectId1',
        (SELECT id FROM organization WHERE name LIKE '默认组织'), '1684747668321', '1684747668336');

replace into test_resource_pool(id, name, type, description, enable, create_time, update_time, create_user, server_url, all_org, deleted)
values ('resourcePoolId', 'resourcePoolName', 'node', 'resourcePoolDescription', 1, unix_timestamp() * 1000,
        unix_timestamp() * 1000, 'admin', 'http://localhost:8080', 1, 0);
replace into test_resource_pool(id, name, type, description, enable, create_time, update_time, create_user, server_url, all_org, deleted)
values ('resourcePoolId1', 'resourcePoolName1', 'node', 'resourcePoolDescription', 1, unix_timestamp() * 1000,
        unix_timestamp() * 1000, 'admin', 'http://localhost:8080', 1, 0);
replace into project_test_resource_pool(project_id, test_resource_pool_id) value ('projectId', 'resourcePoolId');
replace into project_test_resource_pool(project_id, test_resource_pool_id) value ('projectId', 'resourcePoolId1');
replace into test_resource_pool_organization(id , test_resource_pool_id, org_id) value (UUID_SHORT(),'resourcePoolId', '100001');


replace into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                  last_project_id, create_user, update_user)
VALUES ('test-user-key', 'test-user-key', 'test-user-key@metersphere.io', MD5('test-user-key@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, '100001', '', 'LOCAL', '100001100001', 'admin', 'admin');

replace INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES ('test-user-key', 'test-user-key', 'admin', 'system',
       'system', '1684747668375', 'admin');
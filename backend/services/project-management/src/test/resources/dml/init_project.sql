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
                 last_project_id, create_user, update_user,deleted)
VALUES ('delete', 'test2', 'deleted@metersphere.io', MD5('deleted@metersphere.io'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin',1);
replace INTO organization(id,num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('organizationId',null, 'test-org', 'project', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);


# 插入测试数据  给组织增加成员
replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user )VALUES ('user_role_relation1','admin1','org_admin','100001','100001','1684747668321','admin');
replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user )VALUES ('user_role_relation2','delete','org_admin','100001','100001','1684747668321','admin');
replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user )VALUES ('user_role_relation6','delete','project_member','projectId1','100001','1684747668321','admin');
replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user )VALUES ('user_role_relation3','admin1','project_member','projectId1','100001','1684747668321','admin');
replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user )VALUES ('user_role_relation4','admin1','project_member','projectId2','100001','1684747668321','admin');
replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user )VALUES ('user_role_relation5','admin1','project_member','projectId3','100001','1684747668321','admin');
replace INTO user_role_permission(id, role_id, permission_id) VALUES ('user_role_permission1','project_member','PROJECT_BASE_INFO:READ');

#插入测试数据   环境
replace INTO environment (id, name, project_id, create_user, update_user, create_time, update_time) VALUES ('environmentId1', '环境1', 'projectId1', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);

replace into test_resource_pool(id, name, type, description, enable, create_time, update_time, create_user, server_url, all_org, deleted)
values ('resourcePoolId', 'resourcePoolName', 'node', 'resourcePoolDescription', 1, unix_timestamp() * 1000,
        unix_timestamp() * 1000, 'admin', 'http://localhost:8080', 1, 0);
replace into test_resource_pool(id, name, type, description, enable, create_time, update_time, create_user, server_url, all_org, deleted)
values ('resourcePoolId1', 'resourcePoolName1', 'node', 'resourcePoolDescription', 1, unix_timestamp() * 1000,
        unix_timestamp() * 1000, 'admin', 'http://localhost:8080', 1, 0);
replace into project_test_resource_pool(project_id, test_resource_pool_id) value ('projectId', 'resourcePoolId');
replace into project_test_resource_pool(project_id, test_resource_pool_id) value ('projectId', 'resourcePoolId1');
replace into test_resource_pool_organization(id , test_resource_pool_id, org_id) value (UUID_SHORT(),'resourcePoolId', '100001');
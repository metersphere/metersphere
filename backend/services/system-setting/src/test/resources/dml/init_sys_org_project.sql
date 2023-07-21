# 插入测试数据
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('sys_org_projectId', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), 'sys_org_projectId', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('sys_org_projectId1', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), 'sys_org_projectId1', '系统默认创建的项目', 'test', 'test', unix_timestamp() * 1000, unix_timestamp() * 1000);




# 插入测试数据
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '组织下项目1', '组织下项目1', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '组织下项目2', '组织下项目2', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '组织下项目3', '组织下项目3', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '组织下项目4', '组织下项目4', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '组织下项目5', '组织下项目5', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('sys_org_projectId', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '组织下项目6', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('sys_org_projectId1', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '组织下项目7', '系统默认创建的项目', 'test', 'test', unix_timestamp() * 1000, unix_timestamp() * 1000);




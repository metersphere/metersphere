# 插入测试数据
INSERT INTO `project` VALUES ('projectId1', null, '3a5b1bd3-05e5-11ee-ad96-0242ac1e0a02', '默认项目2', '系统默认创建的项目', 1686219258000, 1686219258000, 'admin', 1683464436000, 1, NULL, true);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, delete_time,deleted) VALUES ('projectId6', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目6', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000, 1683464436000 , 1);

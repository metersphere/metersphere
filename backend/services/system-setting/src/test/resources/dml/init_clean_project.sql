# 插入测试数据
INSERT INTO `project` VALUES ('projectId1', null, '3a5b1bd3-05e5-11ee-ad96-0242ac1e0a02', '默认项目2', '系统默认创建的项目', 1686219258000, 1686219258000, 'admin', 1683464436000, 1, NULL, true);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, delete_time,deleted) VALUES ('projectId6', null, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目6', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000, 1683464436000 , 1);
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('default-pro-role-delete-id', 'default-pro-role-delete', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'projectId6');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'admin', 'default-pro-role-delete-id', 'projectId6', (SELECT id FROM organization WHERE name LIKE '默认组织'), UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUE
    (uuid(), 'default-pro-role-delete-id', 'ORGANIZATION_USER_ROLE:READ');

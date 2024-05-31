-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 项目成员权限初始化(遗漏)
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_GROUP:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_MESSAGE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_TEST_PLAN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_API:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_BUG:READ');

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
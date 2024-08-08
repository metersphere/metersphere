-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 项目管理员、项目成员增加权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+EXPORT');

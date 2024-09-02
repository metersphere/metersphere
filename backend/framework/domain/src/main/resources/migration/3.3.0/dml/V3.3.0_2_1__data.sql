-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 组织管理员增加权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT_MEMBER_UPDATE');


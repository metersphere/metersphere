-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 组织管理员增加权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT_MEMBER_UPDATE');

-- 初始化默认参数文件上传大小
INSERT INTO system_parameter (param_key, param_value, type) VALUES ('upload.file.size', '50', 'text');


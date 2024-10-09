-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 组织管理员增加权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT_MEMBER_UPDATE');
-- 项目管理员增加导出权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_REPORT:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_REPORT:READ+EXPORT');

-- 初始化默认参数文件上传大小
INSERT INTO system_parameter (param_key, param_value, type) VALUES ('upload.file.size', '50', 'text');

-- 初次安装初始化用户缺少cft_token字段值
UPDATE user SET cft_token = '/sU3a0dtnm5Ykmyj8LfCjA==' WHERE id = 'admin';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
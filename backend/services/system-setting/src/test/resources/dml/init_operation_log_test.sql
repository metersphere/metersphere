-- 初始化日志记录
INSERT INTO operation_log(`id`, `project_id`, `organization_id`, `create_time`, `create_user`, `source_id`, `method`, `type`, `module`, `content`, `path`) VALUES (uuid(), 'system', '', 1689141859000, 'admin', '1', 'post', 'add', 'SYSTEM_PARAMETER_SETTING', '认证配置', '/system/authsource/add');


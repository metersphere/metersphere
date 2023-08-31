SET SESSION innodb_lock_wait_timeout = 7200;
-- 项目 默认勾选
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_UI_SCENARIO:READ+SCHEDULE', 'PROJECT_UI_SCENARIO'
FROM `group`
WHERE type = 'PROJECT' AND id != 'read_only';
SET SESSION innodb_lock_wait_timeout = DEFAULT;

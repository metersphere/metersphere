SET SESSION innodb_lock_wait_timeout = 7200;

-- 处理兼容数据，角色原本有测试计划报告删除的权限，则添加报告编辑的权限
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
SELECT uuid(), group_id, 'PROJECT_TRACK_REPORT:READ+EDIT', 'PROJECT_TRACK_REPORT'
FROM user_group_permission WHERE permission_id = 'PROJECT_TRACK_REPORT:READ+DELETE';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

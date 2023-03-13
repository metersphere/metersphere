SET SESSION innodb_lock_wait_timeout = 7200;

-- 同时存在用例编辑和用例删除的用户组赋予脑图操作的权限
INSERT INTO user_group_permission
SELECT UUID(), group_id, 'PROJECT_TRACK_CASE_MINDER:OPERATE', 'PROJECT_TRACK_CASE' FROM(
   SELECT group_id, count(permission_id) as permissionCount FROM user_group_permission
   WHERE permission_id in ('PROJECT_TRACK_CASE:READ+EDIT', 'PROJECT_TRACK_CASE:READ+DELETE')
   GROUP BY group_id
   HAVING permissionCount = 2
) group_tmp;

SET SESSION innodb_lock_wait_timeout = DEFAULT;

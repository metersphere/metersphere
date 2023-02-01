SET SESSION innodb_lock_wait_timeout = 7200;
-- 清理超级用户组权限信息，改为默认拥有全部权限
delete from user_group_permission where group_id = 'super_group';
SET SESSION innodb_lock_wait_timeout = DEFAULT;

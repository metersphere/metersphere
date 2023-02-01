SET SESSION innodb_lock_wait_timeout = 7200;
-- 修复数据
update test_plan set repeat_case = 0 where repeat_case is null;

alter table environment_group_project drop column description;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

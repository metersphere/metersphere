SET SESSION innodb_lock_wait_timeout = 7200;
alter table test_plan drop column project_id;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

SET SESSION innodb_lock_wait_timeout = 7200;
alter table user add last_project_id varchar(50) null;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

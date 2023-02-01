SET SESSION innodb_lock_wait_timeout = 7200;
alter table project add tapd_id varchar(50) null;
alter table project add jira_key varchar(50) null;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

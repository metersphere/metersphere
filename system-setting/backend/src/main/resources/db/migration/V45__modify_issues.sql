SET SESSION innodb_lock_wait_timeout = 7200;
alter table issues drop primary key;
alter table issues
	add constraint issues_pk
		primary key (id, platform);
SET SESSION innodb_lock_wait_timeout = DEFAULT;

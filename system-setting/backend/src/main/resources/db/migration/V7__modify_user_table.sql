SET SESSION innodb_lock_wait_timeout = 7200;
alter table user add source varchar(50) null;

update user set source = 'LOCAL' where source is null;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

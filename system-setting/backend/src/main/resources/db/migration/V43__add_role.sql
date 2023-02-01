SET SESSION innodb_lock_wait_timeout = 7200;
INSERT INTO role (id, name, description, type, create_time, update_time)
VALUES ('org_member', '组织成员', NULL, NULL, unix_timestamp() * 1000, unix_timestamp() * 1000);
SET SESSION innodb_lock_wait_timeout = DEFAULT;

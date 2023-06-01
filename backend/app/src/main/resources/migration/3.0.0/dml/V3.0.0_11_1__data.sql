-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

insert into user(id, name, email, password, status, create_time, update_time, language, last_workspace_id, phone, source, last_project_id, create_user)
VALUES ('admin', 'Administrator', 'admin@metersphere.io', MD5('metersphere'), '1', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin');

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
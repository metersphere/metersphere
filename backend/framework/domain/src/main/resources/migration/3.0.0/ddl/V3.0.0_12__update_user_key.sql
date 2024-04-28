-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE user_key MODIFY COLUMN description VARCHAR(1000);

ALTER TABLE api_definition_mock ADD COLUMN status_code INT(50) ;

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;



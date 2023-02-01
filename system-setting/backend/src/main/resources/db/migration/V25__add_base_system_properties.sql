SET SESSION innodb_lock_wait_timeout = 7200;
INSERT into system_parameter values('base.url', 'http://localhost:8081', 'text', 1);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

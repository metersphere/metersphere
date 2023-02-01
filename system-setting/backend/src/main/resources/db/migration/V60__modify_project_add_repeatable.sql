SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE project ADD repeatable tinyint(1) DEFAULT null;
SET SESSION innodb_lock_wait_timeout = DEFAULT;

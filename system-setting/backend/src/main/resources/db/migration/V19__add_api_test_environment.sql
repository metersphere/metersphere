SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE `api_test_environment` ADD `hosts` longtext  COMMENT 'hosts ';
SET SESSION innodb_lock_wait_timeout = DEFAULT;

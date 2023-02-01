SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE api_scenario CHANGE tag_id tags varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'tag list';
SET SESSION innodb_lock_wait_timeout = DEFAULT;

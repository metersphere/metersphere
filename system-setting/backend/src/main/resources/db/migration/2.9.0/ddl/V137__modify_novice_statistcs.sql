SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE novice_statistics MODIFY COLUMN data_option varchar(9000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'data option (JSON format)' AFTER guide_num;
ALTER TABLE novice_statistics ADD COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT '是否显示，0不显示1显示' AFTER data_option;
ALTER TABLE novice_statistics ADD UNIQUE INDEX `uk_id_user_id`(`id`, `user_id`) USING BTREE;
ALTER TABLE novice_statistics ADD UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
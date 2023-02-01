SET SESSION innodb_lock_wait_timeout = 7200;

-- init sql
-- 工单名称 v26_create_index
-- 创建人 guoyuqi
ALTER table issues ADD INDEX project_id_index(project_id);

ALTER table issues ADD INDEX creator_index(creator);

ALTER table custom_field ADD INDEX global_index(global);

ALTER table custom_field ADD INDEX scene_index(scene);

ALTER table custom_field ADD INDEX name_index(name);


SET SESSION innodb_lock_wait_timeout = DEFAULT;
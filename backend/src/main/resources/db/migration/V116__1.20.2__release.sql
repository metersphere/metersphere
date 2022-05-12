ALTER TABLE `user` ADD selenium_server varchar(255) DEFAULT '';

-- start 2022.05.11 jar包支持项目级别和工作空间级别可见
ALTER TABLE jar_config ADD resource_id varchar(50) NOT NULL COMMENT '资源所属的工作空间或者项目Id';
ALTER TABLE jar_config ADD resource_type VARCHAR(20) NOT NULL COMMENT '资源的所属范围 WORKSPACE，PROJECT';

-- 处理旧数据，把旧数据全部设置成工作空间级别
INSERT INTO jar_config (id, name, file_name, creator, modifier, `path`, enable, description, create_time, update_time, resource_id, resource_type)
SELECT UUID() AS id, j.name, j.file_name, j.creator, j.modifier, j.`path`, j.enable, j.description, j.create_time, j.update_time, w.id AS resource_id, 'WORKSPACE' AS resource_type
FROM jar_config j JOIN workspace w;
-- 删除无用旧数据
DELETE FROM jar_config WHERE resource_id = '';
-- end

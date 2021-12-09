-- 新增字段
ALTER TABLE `swagger_url_project` ADD COLUMN `config` longtext COMMENT '鉴权配置信息' AFTER `mode_id`;
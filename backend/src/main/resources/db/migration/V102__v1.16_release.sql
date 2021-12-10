-- 新增字段
ALTER TABLE `swagger_url_project` ADD COLUMN `config` longtext COMMENT '鉴权配置信息' AFTER `mode_id`;

-- 第三方平台模板
ALTER TABLE project ADD platform varchar(20) DEFAULT 'Local' NOT NULL COMMENT '项目使用哪个平台的模板';
ALTER TABLE project ADD third_part_template tinyint(1) DEFAULT 0 NULL COMMENT '是否使用第三方平台缺陷模板';

-- 处理历史数据
UPDATE issue_template SET platform = 'Local' WHERE platform = 'metersphere';
UPDATE project p JOIN issue_template it on p.issue_template_id = it.id SET p.platform = it.platform;
UPDATE custom_field SET `type` = 'date' WHERE `type` = 'data';

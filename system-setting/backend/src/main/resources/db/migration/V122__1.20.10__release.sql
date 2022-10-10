-- 初始化 sql
-- V122_2-1-0_swagger_url_project

ALTER TABLE swagger_url_project ADD COLUMN cover_module TINYINT(1) DEFAULT 0 COMMENT '是否覆盖模块';
update swagger_url_project set mode_id='incrementalMerge' where mode_id='不覆盖';

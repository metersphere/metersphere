-- 工单名称
-- V122_1-20-14_add_feild_scenario_report
-- 创建人
-- jianguo
ALTER TABLE api_scenario_report ADD COLUMN md5_scenario_id VARCHAR(255) generated always AS (MD5(scenario_id)) COMMENT 'scenario_id的MD5数据，自动生成不需要人工维护，用于针对scenario_id列的统计' , ADD INDEX index_md5_scenario_id(md5_scenario_id);
ALTER TABLE `api_scenario_report` ADD INDEX index_create_time (`create_time`);

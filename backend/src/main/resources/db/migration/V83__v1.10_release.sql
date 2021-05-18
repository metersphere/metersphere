-- load_test_report
ALTER TABLE load_test_report
    ADD project_id VARCHAR(50) NULL;

ALTER TABLE load_test_report
    ADD test_name VARCHAR(64) NULL;

ALTER TABLE load_test_report
    ADD jmx_content LONGTEXT NULL;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.project_id = load_test.project_id;

UPDATE load_test_report JOIN load_test ON load_test.id = load_test_report.test_id
SET load_test_report.test_name = load_test.name;
-- schedule
alter table schedule
    add config VARCHAR(500) null;


--  自动清理调试用的body文件
-- INSERT INTO schedule (id,`key`,`type`,value,`group`,job,enable,resource_id,user_id,workspace_id,create_time,update_time,project_id,name)
-- VALUES ('f7f99f50-850b-41b2-8cb9-edee70669c4b','f7f99f50-850b-41b2-8cb9-edee70669c4b','CRON','0 0 4 * * ?','CLEAR_JOB','io.metersphere.job.sechedule.ClearJob',1,'system','admin','system',unix_timestamp() * 1000,unix_timestamp() * 1000,'system','ClearJob');

CREATE TABLE `operating_log` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `project_id` varchar(50) NOT NULL COMMENT 'Project ID',
	`oper_method` varchar(500) DEFAULT NULL COMMENT 'operating method',
	`create_user` varchar(100) DEFAULT NULL COMMENT 'source create u',
  `oper_user` varchar(50) DEFAULT NULL COMMENT 'operating user id',
	`source_id` varchar(6000) DEFAULT NULL COMMENT 'operating source id',
  `oper_type` varchar(100) DEFAULT NULL COMMENT 'operating type',
  `oper_module` varchar(64) DEFAULT NULL COMMENT 'operating module',
  `oper_title` varchar(6000) DEFAULT NULL COMMENT 'operating title',
  `oper_path` varchar(500) DEFAULT NULL COMMENT 'operating path',
	`oper_content` longtext COMMENT 'operating content',
	`oper_params` longtext COMMENT 'operating params',
  `oper_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- add all table create_user
ALTER TABLE api_definition
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_module
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_scenario
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_scenario_module
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_scenario_report
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case_node
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE project
    ADD create_user VARCHAR(100) NULL;

-- system_parameter
UPDATE system_parameter
SET param_key = 'base.prometheus.host'
WHERE param_key = 'prometheus.host';

-- load_test_report
alter table load_test_report
    add test_start_time BIGINT(13) null;

alter table load_test_report
    add test_end_time BIGINT(13) null;

alter table load_test_report
    add test_duration BIGINT(13) null;

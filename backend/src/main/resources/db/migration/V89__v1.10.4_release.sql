-- 添加版本号
ALTER TABLE api_scenario
    ADD version INT(10) DEFAULT 0 NULL COMMENT '版本号';

ALTER TABLE load_test
    ADD scenario_version INT(10) DEFAULT 0 NULL COMMENT '关联的接口自动化版本号';

ALTER TABLE load_test
    ADD scenario_id varchar(255) NULL COMMENT '关联的场景自动化ID';

-- 测试计划用例列表添加缺陷数

alter table test_plan_test_case
    add issues_count int(10) DEFAULT 0 null;
-- 对接Jira等平台认证信息
ALTER TABLE `user`
    ADD platform_info LONGTEXT NULL COMMENT ' 其他平台对接信息';

ALTER TABLE issues ADD platform_status varchar(50) NULL COMMENT '第三方平台状态';

-- 定时同步缺陷
INSERT INTO schedule (id,`key`,`type`,value,`group`,job,enable,resource_id,user_id,workspace_id,create_time,update_time,project_id,name)
VALUES ('7a23d4db-9909-438d-9e36-58e432c8c4ae','ISSUE_SYNC','CRON','0 0 3 * * ?','ISSUE_SYNC','io.metersphere.job.sechedule.IssueSyncJob',1,'system','admin','system',unix_timestamp() * 1000,unix_timestamp() * 1000,'system','ISSUE_SYNC');

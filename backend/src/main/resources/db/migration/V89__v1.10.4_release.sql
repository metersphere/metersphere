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

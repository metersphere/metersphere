-- V128__2-1-0_update_api_scenario_load_report
--
ALTER TABLE `api_definition_exec_result`
    ADD `relevance_test_plan_report_id` varchar(50) COMMENT '关联的测试计划报告ID（可以为空)';

ALTER TABLE `api_scenario_report`
    ADD `relevance_test_plan_report_id` varchar(50) COMMENT '关联的测试计划报告ID（可以为空)';

ALTER TABLE `load_test_report`
    ADD `relevance_test_plan_report_id` varchar(50) COMMENT '关联的测试计划报告ID（可以为空)';


-- V128__2-1-0_init_permission
--
INSERT INTO user_group_permission(id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_TRACK_HOME:READ', 'PROJECT_TRACK_HOME'
FROM `group`
WHERE type = 'PROJECT';


INSERT INTO user_group_permission(id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_API_HOME:READ', 'PROJECT_API_HOME'
FROM `group`
WHERE type = 'PROJECT';


INSERT INTO user_group_permission(id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_PERFORMANCE_HOME:READ', 'PROJECT_PERFORMANCE_HOME'
FROM `group`
WHERE type = 'PROJECT';

-- V2_1_create_API_sync_rule
--
CREATE TABLE api_sync_rule_relation
(
    id                    varchar(50)          NOT NULL,
    resource_id           varchar(50)          NOT NULL COMMENT '来源id',
    resource_type         varchar(50)          NOT NULL COMMENT '来源类型',
    show_update_rule      tinyint(1) DEFAULT 0 NULL COMMENT '是否显示',
    api_sync_case_request longtext             NULL COMMENT '同步规则',
    case_creator          tinyint(1) DEFAULT 1 NULL,
    scenario_creator      tinyint(1) DEFAULT 1 NULL,
    sync_case             tinyint(1) DEFAULT 0 NULL COMMENT '是否同步用例',
    send_notice           tinyint(1) DEFAULT 0 NULL COMMENT '是否发送通知',
    PRIMARY KEY (`id`),
    INDEX `resource_id_index` (`resource_id`)
) ENGINE = INNODB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- V129__2-1-0_update_test_plan_report
--
ALTER TABLE test_plan_report
    ADD COLUMN `is_ui_scenario_executing` TINYINT(1) DEFAULT NULL COMMENT 'is Ui Scenario executing' AFTER is_scenario_executing;


-- V129__2-1-0_add_index
--
ALTER TABLE `api_definition_exec_result`
    ADD INDEX index_relevance_test_plan_report_id (`relevance_test_plan_report_id`);
ALTER TABLE `api_scenario_report`
    ADD INDEX index_relevance_test_plan_report_id (`relevance_test_plan_report_id`);
ALTER TABLE `load_test_report`
    ADD INDEX index_relevance_test_plan_report_id (`relevance_test_plan_report_id`);

-- V129__2-1-0_drop_useless_column
--
ALTER TABLE test_case DROP COLUMN other_test_name;
ALTER TABLE test_plan DROP COLUMN test_case_match_rule;
ALTER TABLE test_plan DROP COLUMN executor_match_rule;

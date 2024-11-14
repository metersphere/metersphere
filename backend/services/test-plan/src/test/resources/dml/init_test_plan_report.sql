-- 计划测试数据
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('test-plan-id-for990', 100001, '100001100001', 'NONE', '1', '测试一下计划-990', 'PREPARED', 'TEST_PLAN', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('test-plan-id-for991', 100002, '100001100001', 'NONE', '1', '测试一下计划-991', 'PREPARED', 'GROUP', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('test-plan-id-for992', 100003, '100001100001', 'NONE', '1', '测试一下计划-992', 'PREPARED', 'TEST_PLAN', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');

-- 计划报告测试数据
INSERT INTO `test_plan_report`(`id`, `test_plan_id`, `name`, `create_user`, `create_time`, `start_time`, `end_time`, `trigger_mode`, `exec_status`, `result_status`, `pass_threshold`, `pass_rate`, `project_id`, `integrated`, `deleted`, `parent_id`, `test_plan_name`) VALUES
('test-plan-report-id-1', 'test-plan-id-for991', '测试一下计划报告1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', 'SUCCESS', '99.99', 100.00, '100001100001', 0, 0, null, '测试一下计划-991'),
('test-plan-report-id-2', 'test-plan-id-for991', '测试一下计划报告1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001', 0, 0, null, '测试一下计划-991'),
('test-plan-report-id-3', 'test-plan-id-for992', '测试一下计划报告3', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001',1, 0, null, '测试一下计划-992'),
('test-plan-report-id-4', 'test-plan-id-for992', '测试一下计划报告4', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001', 1, 0, null, '测试一下计划-992'),
('test-plan-report-id-5', 'test-plan-id-for992', '测试一下计划报告5', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001', 1, 0, null, '测试一下计划-992'),
('test-plan-report-id-6', 'test-plan-id-for992', '测试一下计划报告6', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001', 0, 0, 'test-plan-report-id-5', '测试一下计划-992'),
('test-plan-report-id-7', 'test-plan-id-for992', '测试一下计划报告7', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001', 1, 0, null, '测试一下计划-992'),
('test-plan-report-id-8', 'test-plan-id-for992', '测试一下计划报告8', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001', 0, 0, 'test-plan-report-id-7', '测试一下计划-992'),
('test-plan-report-id-9', 'test-plan-id-for992', '测试一下计划报告9', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00, '100001100001', 0, 1, null, '测试一下计划-992');

INSERT INTO `test_plan_report_summary` (`id`, `functional_case_count`, `api_case_count`, `api_scenario_count`, `bug_count`, `test_plan_report_id`, `summary`, `plan_count`) VALUES
('test-plan-report-summary-1', 0, 0, 0, 0, 'test-plan-report-id-5', '', 0),
('test-plan-report-summary-2', 0, 0, 0, 0, 'test-plan-report-id-6', '', 0),
('test-plan-report-summary-3', 0, 0, 0, 0, 'test-plan-report-id-7', '', 0),
('test-plan-report-summary-4', 0, 0, 0, 0, 'test-plan-report-id-8', '', 0),
('test-plan-report-summary-5', 0, 0, 0, 0, 'test-plan-report-id-9', '', 0);

INSERT INTO `test_plan_report_function_case` (`id`, `test_plan_report_id`, `test_plan_function_case_id`, `function_case_id`, `function_case_num`, `function_case_name`, `function_case_execute_result`, `test_plan_collection_id`, `pos`) VALUES
('report-function-case-1', 'test-plan-report-id-6', 'plan-function-case-1', 'function-case-1', 10000,  'function-case-oasis', 'PENDING', 'NONE', 4096);

INSERT INTO `test_plan_report_api_case` (`id`, `test_plan_report_id`, `test_plan_collection_id`, `test_plan_api_case_id`, `api_case_id`, `api_case_num`, `api_case_name`, `pos`) VALUES
('report-api-case-1', 'test-plan-report-id-6', 'NONE', 'plan-api-case-1', 'api-case-1', 10000,  'api-case-oasis', 4096);

INSERT INTO `test_plan_report_api_scenario` (`id`, `test_plan_report_id`, `test_plan_collection_id`, `test_plan_api_scenario_id`, `api_scenario_id`, `api_scenario_num`, `api_scenario_name`, `pos`) VALUES
('report-api-scenario-1', 'test-plan-report-id-6', 'NONE', 'plan-api-scenario-1', 'api-scenario-1', 10000,  'api-scenario-oasis', 4096);

INSERT INTO `test_plan_report_bug` (`id`, `test_plan_report_id`, `bug_id`, `bug_num`, `bug_title`, `bug_case_count`) VALUES
('report-bug-1', 'test-plan-report-id-6', 'plan-bug-1', 10000, 'bug-1', 1);

-- 计划报告分享信息
INSERT INTO project_application (`project_id`, `type`, `type_value`) VALUES
    ('100001100001', 'TEST_PLAN_SHARE_REPORT', '1D');
INSERT INTO `share_info`(`id`, `create_time`, `create_user`, `update_time`, `share_type`, `custom_data`, `lang`, `project_id`) VALUES
    ('share-1', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'TEST_PLAN_SHARE_REPORT', 0x31303531363635363936353436383137, 'zh_CN', '100001100001');

-- 功能用例执行信息
INSERT INTO test_plan_case_execute_history (`id`, `test_plan_case_id`, `test_plan_id`, `case_id`, `status`, `content`, `steps`, `deleted`, `notifier`, `create_user`, `create_time`) VALUES
    ('execute-his-1', 'test-plan-case-id-for-oasis', 'test-plan-id-for-oasis', 'case-id-for-oasis', 'PENDING', null, null, 0, null, 'admin', UNIX_TIMESTAMP()),
    ('execute-his-2', 'test-plan-case-id-for-oasis', 'test-plan-id-for-oasis', 'case-id-for-oasis', 'PENDING', '1', '1', 0, null, 'admin', UNIX_TIMESTAMP());

-- 任务-报告关联表
INSERT INTO exec_task (id, num, task_name, status, case_count, result, task_type, trigger_mode, project_id, organization_id, create_time, create_user, start_time, end_time, integrated) VALUE
    ('task-id-1', 100001, '任务-1', 'PENDING', 1, 'SUCCESS', 'TEST_PLAN', 'MANUAL', '100001100001', '100001', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO api_report_relate_task (task_resource_id, report_id) VALUE
    ('task-id-1', 'test-plan-report-id-6');
INSERT INTO exec_task (id, num, task_name, status, case_count, result, task_type, trigger_mode, project_id, organization_id, create_time, create_user, start_time, end_time, integrated) VALUE
    ('task-id-2', 100001, '任务-1', 'PENDING', 1, 'SUCCESS', 'TEST_PLAN', 'MANUAL', '100001100001', '100001', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
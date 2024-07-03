replace INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('test_plan_id_1', 5000, '100001100001', 'NONE', '1', '测试一下计划', 'PREPARED', 'TEST_PLAN', NULL,
        1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');

replace INTO `test_plan_report`(`id`, `test_plan_id`, `name`, `create_user`, `create_time`, `start_time`, `end_time`, `trigger_mode`, `exec_status`, `result_status`, `pass_threshold`, `pass_rate`, `project_id`, `integrated`, `deleted`, `parent_id`, `test_plan_name`)
VALUES
('test-plan-report-id-1', 'test_plan_id_1', '测试一下计划报告1', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', 'SUCCESS', '99.99', 100.00, '100001100001', 1, 0, null, '测试一下计划'),
('test-plan-report-id-2', 'test_plan_id_1', '测试一下计划报告1', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', '-', '99.99', 100.00, '100001100001', 0, 0, null, '测试一下计划'),
('test-plan-report-id-3', 'test_plan_id_1', '测试一下计划报告3', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', '-', '99.99', 100.00, '100001100001',1, 0, null, '测试一下计划'),
('test-plan-report-id-4', 'test_plan_id_1', '测试一下计划报告4', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', '-', '99.99', 100.00, '100001100001', 1, 0, null, '测试一下计划'),
('test-plan-report-id-1-1', 'test_plan_id_1', '测试一下计划报告1-1', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', '-', '99.99', 100.00, '100001100001', 1, 0, 'test-plan-report-id-1', '测试一下计划'),
('test-plan-report-id-1-2', 'test_plan_id_1', '测试一下计划报告1-2', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', '-', '99.99', 100.00, '100001100001', 1, 0, 'test-plan-report-id-1-2', '测试一下计划'),
('test-plan-report-id-1-3', 'test_plan_id_1', '测试一下计划报告1-3', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', '-', '99.99', 100.00, '100001100001', 0, 0, 'test-plan-report-id-1-2', '测试一下计划'),
('test-plan-report-id-1-4', 'test_plan_id_1', '测试一下计划报告1-4', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, 'MANUAL', 'RUNNING', '-', '99.99', 100.00, '100001100001', 1, 0, 'test-plan-report-id-1-4', '测试一下计划');



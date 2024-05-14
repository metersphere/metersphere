-- 计划测试数据
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('test-plan-id-for990', 100001, '100001100001', 'NONE', '1', '测试一下计划-990', 'PREPARED', 'TEST_PLAN', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('test-plan-id-for991', 100002, '100001100001', 'NONE', '1', '测试一下计划-991', 'PREPARED', 'GROUP', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES ('test-plan-id-for992', 100003, '100001100001', 'NONE', '1', '测试一下计划-992', 'PREPARED', 'TEST_PLAN', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');

-- 计划报告测试数据
INSERT INTO `test_plan_report`(`id`, `test_plan_id`, `name`, `create_user`, `create_time`, `start_time`, `end_time`, `trigger_mode`, `exec_status`, `result_status`, `pass_threshold`, `pass_rate`) VALUES
('test-plan-report-id-1', 'test-plan-id-for991', '测试一下计划报告1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00),
('test-plan-report-id-2', 'test-plan-id-for991', '测试一下计划报告1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00),
('test-plan-report-id-3', 'test-plan-id-for992', '测试一下计划报告3', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00),
('test-plan-report-id-4', 'test-plan-id-for992', '测试一下计划报告4', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'MANUAL', 'PENDING', '-', '99.99', 100.00);
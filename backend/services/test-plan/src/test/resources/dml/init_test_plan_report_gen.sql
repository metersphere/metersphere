-- 计划的测试数据
INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`) VALUES
('plan_id_for_gen_report', 100001, '100001100001', 'NONE', '1', 'gen-report-plan', 'PREPARED', 'TEST_PLAN', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11'),
('plan_id_for_gen_report_1', 100001, '100001100001', 'NONE', '1', 'gen-report-plan-1', 'PREPARED', 'TEST_PLAN', NULL, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '11');
INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`, `test_planning`) VALUES
('plan_id_for_gen_report', b'0', b'0', 100.00, b'0'),
('plan_id_for_gen_report_1', b'0', b'0', 0.00, b'0');

-- 计划关联用例执行的测试数据
INSERT INTO `test_plan_functional_case` (`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`) VALUES
('plan_id_for_gen_report_case_1', 'plan_id_for_gen_report', 'f1', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'PENDING', 1),
('plan_id_for_gen_report_case_2', 'plan_id_for_gen_report', 'f2', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'SUCCESS', 2),
('plan_id_for_gen_report_case_3', 'plan_id_for_gen_report', 'f3', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'ERROR', 3),
('plan_id_for_gen_report_case_4', 'plan_id_for_gen_report', 'f4', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'BLOCKED', 4),
('plan_id_for_gen_report_case_5', 'plan_id_for_gen_report', 'f5', CURRENT_TIMESTAMP, 'admin', 'admin', CURRENT_TIMESTAMP, 'FAKE_ERROR', 5);

-- 计划关联缺陷的测试数据
INSERT INTO `bug_relation_case`(`id`, `case_id`, `bug_id`, `case_type`, `test_plan_id`, `test_plan_case_id`, `create_user`, `create_time`, `update_time`) VALUES
('test-plan-bug-relate-case-1', 'f1', 'test-plan-bug-id', 'FUNCTIONAL', 'plan_id_for_gen_report', 'f1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
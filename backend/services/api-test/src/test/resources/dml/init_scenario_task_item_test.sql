INSERT INTO `exec_task`(`id`, `num`, `task_name`, `status`, `case_count`, `result`, `task_type`, `trigger_mode`, `project_id`, `organization_id`, `create_time`, `create_user`, `start_time`, `end_time`, `integrated`)
VALUES
    ('scenario_1', 11, '测试任务1', 'SUCCESS', 10, 'SUCCESS', 'FUNCTIONAL', 'API', '100001100001', '100001', 1727676089639, 'wx', 1727676089639, 1727676089639, b'0'),
    ('scenario_2', 22, '测试任务2', 'SUCCESS', 11, 'SUCCESS', 'FUNCTIONAL', 'API', '12345567', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639, b'0'),
    ('scenario_3', 22, '测试任务2', 'SUCCESS', 11, 'SUCCESS', 'FUNCTIONAL', 'API', '12345567', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639, b'1');



INSERT INTO `exec_task_item`(`id`, `task_id`, `resource_id`, `resource_name`, `task_origin`, `status`, `result`, `resource_pool_id`, `resource_pool_node`, `resource_type`, `project_id`, `organization_id`, `thread_id`, `start_time`, `end_time`, `executor`, `create_time`)
VALUES
    ('scenario_1', 'scenario_1', '1', '1', '1', 'SUCCESS', 'SUCCESS', '1', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin', unix_timestamp() * 1000),
    ('scenario_2', 'scenario_2', '1', '2', '3', 'SUCCESS', 'SUCCESS', '2', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin', unix_timestamp() * 1000),
    ('scenario_3', 'scenario_3', '1', '2', '3', 'SUCCESS', 'SUCCESS', '2', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin', unix_timestamp() * 1000);


INSERT INTO `api_report_relate_task`(`task_resource_id`, `report_id`)
VALUES
    ('scenario_1', 'test-scenario-report-id');

INSERT INTO `exec_task`(`id`, `num`, `task_name`, `status`, `case_count`, `result`, `task_type`, `trigger_mode`, `project_id`, `organization_id`, `create_time`, `create_user`, `start_time`, `end_time`, `integrated`)
VALUES
    ('my_1', 1111, '测试1', 'SUCCESS', 10, 'SUCCESS', 'API_SCENARIO', 'API', '100001100001', '100001', 1727676089639, 'wx', 1727676089639, 1727676089639, 1),
    ('my_2', 2222, '测试2', 'SUCCESS', 11, 'SUCCESS', 'API_SCENARIO', 'API', '12345567', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639, 0);


INSERT INTO `exec_task_item`(`id`, `task_id`, `resource_id`, `resource_name`, `task_origin`, `status`, `result`, `resource_pool_id`, `resource_pool_node`, `resource_type`, `project_id`, `organization_id`, `thread_id`, `start_time`, `end_time`, `executor`, `case_id`, `create_time`)
VALUES
    ('my_1', 'my_1', '1', '1', '1', 'SUCCESS', 'SUCCESS', '1', '1', 'API_SCENARIO', '100001100001', '100001', '1', NULL, NULL, 'admin', '1', unix_timestamp() * 1000),
    ('my_2', 'my_2', '1', '2', '3', 'SUCCESS', 'SUCCESS', '2', '1', 'API_SCENARIO', '100001100001', '100001', '1', NULL, NULL, 'admin', '2', unix_timestamp() * 1000);


INSERT INTO `exec_task`(`id`, `num`, `task_name`, `status`, `case_count`, `result`, `task_type`, `trigger_mode`, `project_id`, `organization_id`, `create_time`, `create_user`, `start_time`, `end_time`)
VALUES
    ('pro_1', 1, '测试任务1', 'SUCCESS', 10, 'SUCCESS', 'FUNCTIONAL', 'API', '100001100001', '100001', 1727676089639, 'wx', 1727676089639, 1727676089639),
    ('pro_2', 2, '测试任务2', 'SUCCESS', 11, 'SUCCESS', 'FUNCTIONAL', 'API', '12345567', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639),
    ('pro_3', 3, '测试任务3', 'SUCCESS', 11, 'SUCCESS', 'FUNCTIONAL', 'API', '100001100001', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639);

INSERT INTO `exec_task_item`(`id`, `task_id`, `resource_id`, `resource_name`, `task_origin`, `status`, `result`, `resource_pool_id`, `resource_pool_node`, `resource_type`, `project_id`, `organization_id`, `thread_id`, `start_time`, `end_time`, `executor`)
VALUES
    ('pro_1', 'pro_1', '1', '1', '1', 'SUCCESS', 'SUCCESS', '1', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin'),
    ('pro_2', 'pro_2', '1', '1', '1', 'SUCCESS', 'SUCCESS', '2', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin');

INSERT INTO `test_resource_pool` (`id`, `name`, `type`, `description`, `enable`, `create_time`, `update_time`, `create_user`, `server_url`, `all_org`, `deleted`)
VALUES
    ('1', 'LOCAL', 'Node', '测试资源池', b'1', 1705894549000, 1705894549000, 'admin', NULL, b'1', b'0'),
    ('2', 'LOCAL', 'Kubernetes', 'Kubernetes测试资源池', b'1', 1705894549000, 1705894549000, 'admin', NULL, b'1', b'0');

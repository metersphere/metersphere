INSERT INTO `exec_task`(`id`, `num`, `task_name`, `status`, `case_count`, `result`, `task_type`, `trigger_mode`, `project_id`, `organization_id`, `create_time`, `create_user`, `start_time`, `end_time`, `integrated`)
VALUES
    ('pro_1', 1, '测试任务1', 'SUCCESS', 10, 'SUCCESS', 'FUNCTIONAL', 'API', '100001100001', '100001', 1727676089639, 'wx', 1727676089639, 1727676089639, b'0'),
    ('pro_2', 2, '测试任务2', 'SUCCESS', 11, 'SUCCESS', 'FUNCTIONAL', 'API', '12345567', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639, b'0'),
    ('pro_3', 3, '测试任务3', 'SUCCESS', 11, 'SUCCESS', 'FUNCTIONAL', 'API', '100001100001', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639, b'0'),
    ('pro_4', 4, '测试任务4', 'SUCCESS', 11, 'SUCCESS', 'FUNCTIONAL', 'API_CASE_BATCH', '100001100001', '11234', 1727676089639, 'wx', 1727676089639, 1727676089639, b'1');

INSERT INTO `exec_task_item`(`id`, `task_id`, `resource_id`, `resource_name`, `task_origin`, `status`, `result`, `resource_pool_id`, `resource_pool_node`, `resource_type`, `project_id`, `organization_id`, `thread_id`, `start_time`, `end_time`, `executor`, `create_time`)
VALUES
    ('pro_1', 'pro_1', '1', '1', '1', 'SUCCESS', 'SUCCESS', '1', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin', unix_timestamp() * 1000),
    ('pro_2', 'pro_2', '1', '1', '1', 'SUCCESS', 'SUCCESS', '2', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin', unix_timestamp() * 1000),
    ('pro_4_1', 'pro_4', '1', '1', '1', 'SUCCESS', 'SUCCESS', '2', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin', unix_timestamp() * 1000),
    ('pro_4_2', 'pro_4', '1', '1', '1', 'SUCCESS', 'SUCCESS', '2', '1', 'API_CASE', '100001100001', '100001', '1', NULL, NULL, 'admin', unix_timestamp() * 1000);

INSERT INTO `test_resource_pool` (`id`, `name`, `type`, `description`, `enable`, `create_time`, `update_time`, `create_user`, `server_url`, `all_org`, `deleted`)
VALUES
    ('1', 'LOCAL', 'Node', '测试资源池', b'1', 1705894549000, 1705894549000, 'admin', NULL, b'1', b'0'),
    ('2', 'LOCAL', 'Kubernetes', 'Kubernetes测试资源池', b'1', 1705894549000, 1705894549000, 'admin', NULL, b'1', b'0');


INSERT INTO `schedule` (`id`, `key`, `type`, `value`, `job`, `resource_type`, `enable`, `resource_id`, `create_user`, `create_time`, `update_time`, `project_id`, `name`, `config`, `num`)
VALUES
    ('pro_wx_1', 'wx_key_1', 'cron', '1233213', 'JobClass1', 'API_IMPORT', b'1', '12134', 'admin', 1640776000000, 1640777000000, '100001100001', 'Schedule 1', '{\"param1\": \"value1\", \"param2\": \"value2\"}', 100),
    ('pro_wx_2', 'wx_key_2', 'cron', '1231321231', 'JobClass15', 'BUG_SYNC', b'0', '2234', 'admin', 1640777400000, 1640778400000, '100001100001', 'Schedule 15', '', 101),
    ('wx_3', 'wx_key_3', 'cron', '50 15 10 20 05 ?', 'JobClass22', 'DEMAND_SYNC', b'1', '3234', 'admin', 1640778100000, 1640779100000, '100001100001', 'Schedule 22', '', 102);


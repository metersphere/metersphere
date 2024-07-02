

INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('gyq_plan_1', 5000, 'gyq_plan_project', 'NONE', '1', 'qwe', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'admin', 1714980158000, 'admin', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('gyq_plan_2', 10000, 'gyq_plan_project', 'NONE', '1', 'eeew', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'admin', 1714980158000, 'admin', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');


INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('gyq_plan_project', null, 'organization-associate-case-test', '用例评论项目', '系统默认创建的项目',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO test_plan_api_case(id, test_plan_id, api_case_id, environment_id, last_exec_result, last_exec_report_id, execute_user, create_time, create_user, pos, test_plan_collection_id, last_exec_time)
VALUES ('gyq_wxxx_1', 'gyq_plan_1', 'wxxx_api_case_1', 'gyq_123', NULL, NULL, 'admin', 1716370415311, 'admin', 2, 'gyq_wxxx_4', 1716370415311);

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted)
VALUES ('gyq_plan_api-case-associate-1','test4938131', 'P0', 10000023131, null, 'Underway', 'PENDING', null, 1, 'gyq_plan_project', 'gyq_plan_api_definition_id_1', '100570499574136985', 'test_associate_env_id', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', null, null, false);

INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('gyq_plan_api_definition_id_1', 'gyq_associate_api_definition_id_1', 'HTTP', 'POST','api/test','test-api-status', 1000001, null, 1, 'gyq_plan_project' , 'gyq_associate_test_module', true, 'v1.10','gyq_associate_api_definition_id_1', null, UNIX_TIMESTAMP() * 1000,'admin', UNIX_TIMESTAMP() * 1000,'admin', null,null,false);

INSERT INTO `test_plan_functional_case`(id, test_plan_id, functional_case_id, create_time, create_user, execute_user, last_exec_time, last_exec_result, pos, test_plan_collection_id)
VALUES('gyq_functional_case_1', 'gyq_plan_1', 'functional_case_id', 1716797474979, 'admin', 'admin', 1716866691313, 'SUCCESS', 4096, 'gyq_wxxx_5');


INSERT INTO `test_plan_api_scenario`(id, test_plan_id, api_scenario_id, environment_id, execute_user, last_exec_result, last_exec_report_id, create_time, create_user, pos, test_plan_collection_id, grouped, last_exec_time)
VALUES ('gyq_scenario_case_1', 'gyq_plan_1', 'api_scenario_id', 'gyq_123','admin', 'SUCCESS', 'last_exec_report_id', 1716866691313,'admin',  4096, 'gyq_wxxx_6', b'0', 1716866691313);


INSERT INTO `test_plan_collection`(`id`, `test_plan_id`, `name`, `type`, `environment_id`, `test_resource_pool_id`, `pos`, `create_user`, `create_time`, `parent_id`)
VALUES
    ('gyq_wxxx_1', 'gyq_plan_1', '接口用例', 'API', 'NONE', 'gyq_123_pool', 1, 'admin', 1716370415311, 'NONE'),
    ('gyq_wxxx_2', 'gyq_plan_1', '功能用例', 'FUNCTIONAL', 'gyq_123', 'gyq_123_pool', 2, 'admin', 1716370415311, 'NONE'),
    ('gyq_wxxx_3', 'gyq_plan_1', '场景用例', 'SCENARIO', 'NONE', 'NONE', 3, 'admin', 1716370415311, 'NONE'),
    ('gyq_wxxx_4', 'gyq_plan_1', '接口测试集', 'API', 'NONE', 'gyq_123_pool', 1, 'admin', 1716370415311, 'gyq_wxxx_1'),
    ('gyq_wxxx_5', 'gyq_plan_1', '功能测试集', 'FUNCTIONAL', 'gyq_123', 'gyq_123_pool', 2, 'admin', 1716370415311, 'gyq_wxxx_2'),
    ('gyq_wxxx_6', 'gyq_plan_1', '场景测试集', 'SCENARIO', 'NONE', 'gyq_123_pool', 3, 'admin', 1716370415311, 'gyq_wxxx_3');


INSERT INTO `environment`(`id`, `name`, `project_id`, `create_user`, `update_user`, `create_time`, `update_time`, `mock`, `description`, `pos`)
VALUES ('gyq_123', 'Mock环境', 'wxx_1234', 'admin', 'admin', 1716175907000, 1716175907000, b'1', NULL, 64);

INSERT INTO test_resource_pool (id, name, type, description, enable, create_time, update_time, create_user, all_org, deleted)
VALUES ('gyq_123_pool', '默认资源池', 'Node', '系统初始化资源池', true, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', true, false);


INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`, `case_run_mode`)
VALUES ('gyq_plan_1', b'0', b'0', 100.00, 'PARALLEL');

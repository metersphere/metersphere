INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted,
                         delete_user, delete_time) VALUE
    ('organization-associate-scenario-test', null, 'organization-associate-case-test', 'organization-associate-case-test',
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('project-associate-scenario-test', null, 'organization-associate-scenario-test', '用例评论项目', '系统默认创建的项目',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyq_associate_scenario_id_1', 100, 'TEST_MODULE_ID', 'project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_associate_scenario_id_1', 'STEP', '1111', '', '', 'TEST');

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time, deleted)
VALUES ('associate_gyq_api_scenario_one', 'api_scenario', 'p1', 'test-api-status',  'PENDING', null,1000001, 1,'v1.10', 'associate_gyq_api_scenario_one','project-associate-scenario-test', 'gyq_associate_test_scenario_module', null,null,'admin',  UNIX_TIMESTAMP() * 1000,null,null,'admin', UNIX_TIMESTAMP() * 1000, false),
       ('associate_gyq_api_scenario_two', 'api_scenario_two', 'p1', 'test-api-status',  'PENDING', null,1000001, 1,'v1.10', 'associate_gyq_api_scenario_two','project-associate-scenario-test', 'gyq_associate_test_scenario_module', null,null,'admin',  UNIX_TIMESTAMP() * 1000,null,null,'admin', UNIX_TIMESTAMP() * 1000, false);


INSERT INTO api_scenario_module(id, name,parent_id, project_id, pos, create_time, update_time, update_user, create_user)
VALUES ('gyq_associate_test_scenario_module', 'gyq_associate_test_module', 'NONE', 'project-associate-scenario-test', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin','admin');

INSERT INTO project_version(id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user)
values ('gyq_associate_test_scenario_version_id','project-associate-scenario-test','v1.0.0', null, 'open', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin');



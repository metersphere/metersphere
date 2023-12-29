INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted,
                         delete_user, delete_time) VALUE
    ('gyq-organization-associate-case-test', null, 'gyq-organization-associate-case-test', 'gyq-organization-associate-case-test',
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('gyq-project-associate-case-test', null, 'gyq-organization-associate-case-test', '用例评论项目', '系统默认创建的项目',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyq_associate_functional_case_id_1', 100, 'TEST_MODULE_ID', 'gyq-project-associate-case-test', '100001', '测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_associate_functional_case_id_1', 'STEP', '1111', '', '', 'TEST');

INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted)
VALUES ('gyq_api_case_id_1','gyq_api_case_id_1','P0',1001, null, 'Underway', null, null, 100, 'gyq-project-associate-case-test', 'gyq_api_definition_id_1', 'gyq_version_id', 'gyq_associate_env_id', 1698058347559, 'admin',1698058347559,'admin',null,null,false);



INSERT INTO api_test_case(id, name, priority, num, tags, status, last_report_status, last_report_id, pos, project_id, api_definition_id, version_id, environment_id, create_time, create_user, update_time, update_user, delete_time, delete_user, deleted)
VALUES ('gyq_api_case_id_2','测试查询模块用','P0',1002, null, 'Underway', null, null, 200, 'gyq-project-associate-case-test', 'gyq_api_definition_id_1', 'gyq_version_id', 'gyq_associate_env_id', 1698058347559, 'admin',1698058347559,'admin',null,null,false);


INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('gyq_associate_api_definition_id_1', 'gyq_associate_api_definition_id_1', 'HTTP', 'POST','api/test','test-api-status', 1000001, null, 1, 'gyq-project-associate-case-test' , 'gyq_associate_test_module', true, 'v1.10','gyq_associate_api_definition_id_1', null, UNIX_TIMESTAMP() * 1000,'admin', UNIX_TIMESTAMP() * 1000,'admin', null,null,false);


INSERT INTO api_definition_module(id, name,parent_id, project_id, pos, create_time, update_time, update_user, create_user)
VALUES ('gyq_associate_test_module', 'gyq_associate_test_module', 'NONE', 'gyq-project-associate-case-test', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin','admin');

INSERT INTO project_version(id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user)
values ('gyq_version_id','gyq-project-associate-case-test','v1.0.0', null, 'open', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin');

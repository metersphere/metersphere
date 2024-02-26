
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-case-demand-test', null, 'organization-case-demand-test', 'organization-case-demand-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-case-demand-test', 1001, 'organization-case-demand-test', '用例需求项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('gyq_project-case-demand-test', 5001, '100001', '用例需求项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);



INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, ref_id, scene)
VALUES ('test_template_case_demand_id', 'functional_case_demand_default', '', b'0', 1696992836000, 1696992836000, 'admin', 'PROJECT', 'project-case-demand-test', b'0', NULL, 'FUNCTIONAL');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('DEMAND_TEST_FUNCTIONAL_CASE_ID', 1, 'DEMAND_TEST_MODULE_ID', 'project-case-demand-test', '100001', '关联需求测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'DEMAND_TEST_FUNCTIONAL_CASE_ID', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('DEMAND_TEST_FUNCTIONAL_CASE_ID2', 1, 'DEMAND_TEST_MODULE_ID', 'project-case-demand-test', '100001', '关联需求测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'DEMAND_TEST_FUNCTIONAL_CASE_ID2', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('DEMAND_TEST_FUNCTIONAL_CASE_ID3', 1, 'DEMAND_TEST_MODULE_ID', 'project-case-demand-test', '100001', '关联需求测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'DEMAND_TEST_FUNCTIONAL_CASE_ID3', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('DEMAND_TEST_FUNCTIONAL_CASE_ID4', 1, 'DEMAND_TEST_MODULE_ID', 'project-case-demand-test', '100001', '关联需求测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'DEMAND_TEST_FUNCTIONAL_CASE_ID4', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);


INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('DEMAND_TEST_FUNCTIONAL_CASE_ID5', 1, 'DEMAND_TEST_MODULE_ID', 'gyq_project-case-demand-test', '100001', '关联需求测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'DEMAND_TEST_FUNCTIONAL_CASE_ID5', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('DEMAND_TEST_FUNCTIONAL_CASE_ID', 'gyq_custom_id_demand1', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('DEMAND_TEST_FUNCTIONAL_CASE_ID', 'gyq_custom_id_demand2', '33');



INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id_demand1', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_demand_id');

INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id_demand2', 'level', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_demand_id');

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('DEMAND_TEST_MODULE_ID', 'project-case-demand-test', '测试需求所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');

INSERT INTO project_application (project_id, type, type_value) VALUES
                                                                   ('gyq_project-case-demand-test', 'CASE_RELATED_CASE_ENABLE', 'true'),
                                                                   ('gyq_project-case-demand-test', 'CASE_RELATED_DEMAND_PLATFORM_CONFIG', '{"jiraKey":"TES","jiraDemandTypeId":"10007"}'),
                                                                   ('gyq_project-case-demand-test', 'CASE_RELATED_PLATFORM_KEY', 'jira');
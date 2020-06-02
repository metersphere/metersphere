INSERT INTO user (id, name, email, password, status, create_time, update_time, language, last_workspace_id, last_organization_id, phone)
VALUES ('admin', 'Administrator', 'admin@metersphere.io', md5('metersphere'), '1', 1582597567455, 1582597567455, null, '', null, null);

INSERT INTO user_role (id, user_id, role_id, source_id, create_time, update_time)
VALUES (uuid(), 'admin', 'admin', '1', 1581576575948, 1581576575948);

INSERT INTO role (id, name, description, type, create_time, update_time)
VALUES ('admin', '系统管理员', NULL, NULL, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time)
VALUES ('org_admin', '组织管理员', NULL, NULL, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time)
VALUES ('test_manager', '测试经理', NULL, NULL, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time)
VALUES ('test_user', '测试人员', NULL, NULL, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time)
VALUES ('test_viewer', 'Viewer', NULL, NULL, 1581576575948, 1581576575948);

INSERT INTO organization (id, name, description, create_time, update_time)
VALUES (uuid(), '默认组织', '系统默认创建的组织', 1581576575948, 1581576575948);
INSERT INTO workspace (id, organization_id, name, description, create_time, update_time)
VALUES (uuid(), (select id from organization where name like '默认组织'), '默认工作空间', '系统默认创建的工作空间', 1581576575948, 1581576575948);
INSERT INTO user_role (id, user_id, role_id, source_id, create_time, update_time)
VALUES (uuid(), 'admin', 'org_admin', (select id from organization where name like '默认组织'), 1581576575948, 1581576575948);
INSERT INTO user_role (id, user_id, role_id, source_id, create_time, update_time)
VALUES (uuid(), 'admin', 'test_manager', (select id from workspace where name like '默认工作空间'), 1581576575948, 1581576575948);

INSERT INTO test_resource_pool (id, name, type, description, status, create_time, update_time)
VALUES (uuid(), 'LOCAL', 'NODE', '系统默认创建的本地资源池', 'VALID', 1581576575948, 1581576575948);
INSERT INTO test_resource (id, test_resource_pool_id, configuration, status, create_time, update_time)
VALUES (uuid(), (select id from test_resource_pool where name like 'LOCAL'), 'GgC8aAZVAsiNDdnvp4gobdv1iwAvloLCAaeqb7Ms1VaLzW+iXHsFhGg8ZaPEk53W6xA5A6g+UUUxbKvU2s7yZw==', 'VALID', 1581576575948, 1581576575948);

INSERT INTO test_case_report_template (id, name, content)
VALUES (uuid(), 'default', '{\"components\": [1,2,3,4,5]}');
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('default.language', 'zh_CN', 'text', 5);

INSERT INTO user (id, name, email, password, status, create_time, update_time, language, last_workspace_id, last_organization_id, phone)
VALUES ('admin', 'Administrator', 'admin@fit2cloud.com', md5('fit2cloud'), '1', 1582597567455, 1582597567455, null, '', null, null);

INSERT INTO user_role (id, user_id, role_id, source_id, create_time, update_time)
VALUES (uuid(), 'admin', 'admin', '1', 1581576575948, 1581576575948);

INSERT INTO role (id, name, description, type, create_time, update_time) VALUES ('admin', '系统管理员', null, null, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time) VALUES ('org_admin', '组织管理员', null, null, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time) VALUES ('test_manager', '测试经理', null, null, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time) VALUES ('test_user', '测试人员', null, null, 1581576575948, 1581576575948);
INSERT INTO role (id, name, description, type, create_time, update_time) VALUES ('test_viewer', 'Viewer', null, null, 1581576575948, 1581576575948);


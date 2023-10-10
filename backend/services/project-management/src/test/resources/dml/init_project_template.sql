# 插入测试数据
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-template-test', null, 'organization-template-test', 'organization-template-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-template-test', null, 'organization-template-test', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-template-test-1', null, 'organization-template-test', '默认项目1', '系统默认创建的项目1', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-template-test-2', null, 'organization-template-test', '默认项目2', '系统默认创建的项目2', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);



INSERT INTO custom_field (id, name, scene, type, remark, create_time, update_time, create_user, scope_id) VALUES
                                                                                                              ('custom_field_template_1', 'level', 'FUNCTIONAL', 'SELECT', '等级',  UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-template-test-1'),
                                                                                                              ('custom_field_template_2', 'grade', 'API', 'SELECT', '',  UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-template-test-1'),
                                                                                                              ('custom_field_template_3', 'age', 'UI', 'SELECT', '',  UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-template-test-1'),
                                                                                                              ('custom_field_template_4', 'process', 'ISSUE', 'SELECT', '',  UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-template-test-1'),
                                                                                                              ('custom_field_template_5', 'aa', 'TEST_PLAN', 'SELECT', '',  UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-template-test-1');

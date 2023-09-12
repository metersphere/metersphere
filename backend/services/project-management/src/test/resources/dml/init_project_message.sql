# 插入测试数据
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-message-test', null, 'organization-message-test', 'organization-message-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-message-test', null, 'organization-message-test', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user, deleted) VALUES
                                                                                                                                                                             ('project-message-user-1', 'project-message-user-1', 'project-message-member1@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 0),
                                                                                                                                                                             ('project-message-user-2', 'project-message-user-2', 'project-message-member2@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 0),
                                                                                                                                                                             ('project-message-user-del', 'project-message-user-del', 'project-message-member-del@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES
                                                                                                                (UUID(), 'project-message-user-1', 'project_member', 'project-message-test', 'organization-message-test', UNIX_TIMESTAMP() * 1000, 'admin'),
                                                                                                                (UUID(), 'project-message-user-2', 'project_member', 'project-message-test', 'organization-message-test', UNIX_TIMESTAMP() * 1000, 'admin');



replace INTO project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES ('test_message_robot1', 'test', '测试机器人1', 'IN_SITE', 'NONE', null,  null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);
replace INTO project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES ('test_message_robot2', 'test', '测试机器人2', 'MAIL', 'NONE', null,  null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);





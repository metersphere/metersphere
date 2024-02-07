# 插入测试数据

replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('test1', null, 'organization-message-test', '默认项目1', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('test', null, 'organization-message-test', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

replace INTO project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) values ('message_task_robot1', 'test', '测试机器人1', 'IN_SITE', 'NONE', null,  null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);


replace INTO message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('message_task_id1', 'CASE_EXECUTE_FAKE_ERROR', '["FOLLOW_PEOPLE"]', 'message_task_robot1', 'API_DEFINITION_TASK', 'NONE',  'test', true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_delete');

replace INTO message_task_blob(id, template) VALUES ('message_task_id1', 'message.api_definition_task_case_execute');



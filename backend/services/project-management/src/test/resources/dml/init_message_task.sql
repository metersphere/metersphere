# 插入测试数据

replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('test1', null, 'organization-message-test', '默认项目1', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
replace INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('test', null, 'organization-message-test', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
replace INTO message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable,  create_user, create_time, update_user, update_time) VALUES ('message_task_id', 'CREATE', 'gyq', '测试机器人1', 'API_DEFINITION_TASK', 'NONE',  'test', true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000);
replace INTO message_task_blob(id, template) VALUES ('message_task_id', '测试发消息');

replace INTO message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable,  create_user, create_time, update_user, update_time) VALUES ('message_task_id2', 'UPDATE', 'gyq1', '测试机器人1', 'API_DEFINITION_TASK', 'NONE',  'test', true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000);
replace INTO message_task_blob(id, template) VALUES ('message_task_id2', '测试发消息2');



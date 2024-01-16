INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('aspect_gyq_one', 1000001, 'test_guo', '100001100001', 'test_guo', 'gyq_test_one', 'UN_REVIEWED', null, 'text',
        10001, '111', 'aspect_gyq_one', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('aspect_gyq_two', 1000001, 'test_guo', '100001100001', 'test_guo', 'gyq_test_two', 'UN_REVIEWED', null, 'text',
        10001, '111', 'aspect_gyq_two', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('aspect-member-user-guo', 'aspect-member-user-guo', 'aspect-member-user-guo@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'aspect-member-user-guo', 'org_member', '100001', '100001', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'aspect-member-user-guo', 'project_admin', '100001100001', '100001', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_permission(id, role_id, permission_id)
VALUES ('aspect_user_role_guo_permission1', 'project_admin', 'FUNCTIONAL_CASE_COMMENT:READ+ADD'),
       ('aspect_user_role_guo_permission2', 'project_admin', 'FUNCTIONAL_CASE:READ+ADD');

INSERT INTO custom_field(id, name, scene, type, remark, create_time, update_time, create_user, ref_id, scope_id, internal, enable_option_key, scope_type)
values ('aspect_test_one','aspect_test','FUNCTIONAL', 'INPUT','aspect_test',  UNIX_TIMESTAMP() * 1000,  UNIX_TIMESTAMP() * 1000, 'admin' , 'aspect_test_one', '100001100001',false, false, 'PROJECT');

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('aspect_gyq_one', 'aspect_test_one', 'hello');

INSERT INTO api_definition(id, name, protocol, method, path, status, num, tags, pos, project_id, module_id, latest, version_id, ref_id, description, create_time, create_user, update_time, update_user, delete_user, delete_time, deleted)
VALUES ('aspect_gyq_api_one', 'api_test','HTTP', 'POST','api/test','test-api-status', 1000001, null, 1, '100001100001' , 'test_module', true, 'v1.10','aspect_gyq_api_one', null, UNIX_TIMESTAMP() * 1000,'admin', UNIX_TIMESTAMP() * 1000,'admin', null,null,false);

INSERT INTO api_scenario(id, name, priority, status, last_report_status, last_report_id, num, pos, version_id, ref_id, project_id, module_id, description, tags, create_user, create_time, delete_time, delete_user, update_user, update_time)
VALUES ('aspect_gyq_api_scenario_one', 'api_scenario', 'p1', 'test-api-status',  null, null,1000001, 1,'v1.10', 'aspect_gyq_api_scenario_one','100001100001', 'test_module', null,null,'admin',  UNIX_TIMESTAMP() * 1000,null,null,'admin', UNIX_TIMESTAMP() * 1000);

INSERT INTO test_plan(id, num, project_id, group_id, module_id, name, status, type, tags, create_time, create_user, update_time, update_user, planned_start_time, planned_end_time, actual_start_time, actual_end_time, description)
VALUES ('aspect_gyq_test_plan_one', 5000,'100001100001','NONE','test_plan_module_id','test_plan', 'PREPARED','a', null, UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000,'admin',  UNIX_TIMESTAMP() * 2000,
        UNIX_TIMESTAMP() * 3000, UNIX_TIMESTAMP() * 2000, UNIX_TIMESTAMP() * 3000, null);


INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, tags, description, create_time, create_user, update_time, update_user)
VALUES ('aspect_gyq_case_review_one','10001','case_review','module_id', '100001100001','PREPARED','SINGLE','0', null, null, null, null, UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('aspect_gyq_project_one', 100, '100001', '测试项目(消息)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('aspect_gyq_three', 1000001, 'test_guo', 'aspect_gyq_project_one', 'test_guo', 'gyq_test_two', 'UN_REVIEWED', null, 'text',
        10001, '111', 'aspect_gyq_two', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

Insert into project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description)
VALUES ('robot_ding_custom_id', 'aspect_gyq_project_one', 'robot_in_ding_custom', 'DING_CUSTOM_ROBOT', 'https://oapi.dingtalk.com/robot/send?access_token=fd963136a4d7eebaaa68de261223089148e62d7519fbaf426626fe3157725b8a', 'CUSTOM', null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, 'robot_in_site_description');
Insert into project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description)
VALUES ('robot_ding_enterprise_id', 'aspect_gyq_project_one', 'robot_in_ding_enterprise', 'DING_ENTERPRISE_ROBOT', 'https://oapi.dingtalk.com/robot/send?access_token=5243ba0bdee4a20f920c169fcad352e3f5c00c62e34c4352194729b6e571c936', 'ENTERPRISE', 'dingxwd71o7kj4qoixo7', 'szmOD9bjGgKtfYk09-Xx2rPdX-xkW4R8Iic0eig_k1D3k95nG4TLKRSpUKUD_f0G', true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, 'robot_in_site_description');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES ('message_robot_ding_id', 'UPDATE', 'CREATE_USER', 'robot_ding_custom_id', 'FUNCTIONAL_CASE_TASK', 'aspect_gyq_two', 'aspect_gyq_project_one', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES ('message_robot_ding_id', 'message.functional_case_task_update');

Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES ('message_robot_ding_en_id', 'UPDATE', 'CREATE_USER', 'robot_ding_enterprise_id', 'FUNCTIONAL_CASE_TASK', 'aspect_gyq_two', 'aspect_gyq_project_one', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES ('message_robot_ding_en_id', 'message.functional_case_task_update');





INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-gyq-case-review-test', null, 'organization-gyq-case-review-test', 'organization-gyq-case-review-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-gyq-case-review-test', null, 'organization-gyq-case-review-test', '用例评审项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-gyq-case-review-test_gt', null, 'organization-gyq-case-review-test', '用例评审项目2', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);


INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, ref_id, scene)
VALUES ('test_template_case_review_gyq_id', 'functional_case_review_gyq', '', b'0', 1696992836000, 1696992836000, 'admin', 'PROJECT', 'project-gyq-case-review-test', b'0', NULL, 'FUNCTIONAL');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试1', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID2', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试2', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID2', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID3', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试3', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID3', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID4', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试4', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID4', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID5', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试5', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID5', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID6', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试6', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID6', 'UN_EXECUTED', false, b'0', b'1', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('CASE_REVIEW_TEST_GYQ_ID', 'gyq_custom_id_review1', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('CASE_REVIEW_TEST_GYQ_ID', 'gyq_custom_id_review2', '33');



INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id_review1', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_review_gyq_id');

INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id_review2', 'level', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_review_gyq_id');

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '测试用例评审所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');

INSERT INTO case_review_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user)
VALUES ('CASE_REVIEW_REAL_MODULE_ID', 'project-gyq-case-review-test', '用例评审所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin'),
       ('CASE_REVIEW_REAL_MODULE_ID2', 'project-gyq-case-review-test', '用例评审所属模块1', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');


INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('gyq_review_test', 'gyq_review_test', 'gyq_review_test@fit2cloud.com', MD5('metersphere'),UNIX_TIMESTAMP() * 1000,UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
       ('gyq_review_test2', 'default-Administrator-1', 'admin-default-user@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false);


INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('review-case-role-id-1', 'review-case-role-1', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-gyq-case-review-test');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('review-case-role-id-2', 'review-case-role-2', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-gyq-case-review-test');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('review-case-role-id-3', 'review-case-role-3', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-gyq-case-review-test');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('review-case-role-id-4', 'review-case-role-4', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'project-gyq-case-review-test');

INSERT INTO user_role_permission (id, role_id, permission_id) VALUE
    (uuid(), 'review-case-role-id-3', 'CASE_REVIEW:READ+REVIEW');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'gyq_review_test', 'review-case-role-id-3', 'project-gyq-case-review-test', 'organization-gyq-case-review-test', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    (UUID(), 'gyq_review_test2', 'review-case-role-id-3', 'project-gyq-case-review-test', 'organization-gyq-case-review-test', UNIX_TIMESTAMP() * 1000, 'admin');



INSERT INTO project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES ('test_case_review_message_robot1', 'project-gyq-case-review-test', '测试机器人1', 'IN_SITE', 'NONE', null,  null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-review_message0', 'CREATE', '["gyq_review_test","CREATE_USER"]', 'test_case_review_message_robot1', 'CASE_REVIEW_TASK', 'NONE', 'project-gyq-case-review-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_create');
INSERT INTO message_task_blob(id, template) VALUES ('case-review_message0', 'message.case_review_task_create');

Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-review_message1', 'UPDATE', '["CREATE_USER", "FOLLOW_PEOPLE", "gyq_review_test2"]', 'test_case_review_message_robot1', 'CASE_REVIEW_TASK', 'NONE', 'project-gyq-case-review-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_update');
INSERT INTO message_task_blob(id, template) VALUES ('case-review_message1', 'message.case_review_task_update');

Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-review_message3', 'DELETE', '["CREATE_USER", "gyq_review_test2"]', 'test_case_review_message_robot1', 'CASE_REVIEW_TASK', 'NONE', 'project-gyq-case-review-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_delete');
INSERT INTO message_task_blob(id, template) VALUES ('case-review_message3', 'message.case_review_task_delete');

Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-review_message4', 'REVIEW_COMPLETED', '["CREATE_USER"]', 'test_case_review_message_robot1', 'CASE_REVIEW_TASK', 'NONE', 'project-gyq-case-review-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_review_completed');
INSERT INTO message_task_blob(id, template) VALUES ('case-review_message4', 'message.case_review_task_review_completed');


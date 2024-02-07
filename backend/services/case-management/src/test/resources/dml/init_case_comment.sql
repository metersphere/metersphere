
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-case-comment-test', null, 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-case-comment-test', null, 'organization-case-comment-test', '用例评论项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-case-comment-test-1', null, 'organization-case-comment-test', '用例评论项目1', '系统默认创建的项目1', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-case-comment-test-2', null, 'organization-case-comment-test', '用例评论项目2', '系统默认创建的项目2', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-case-comment-test-3', null, 'organization-case-comment-test', '用例评论项目3', '系统默认创建的项目3', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES ('test_comment_message_robot1', 'project-case-comment-test', '测试机器人1', 'IN_SITE', 'NONE', null,  null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);



Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message1', 'UPDATE', '["CREATE_USER", "FOLLOW_PEOPLE"]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message1', 'message.functional_case_task_update');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message3', 'DELETE', '["CREATE_USER"]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_delete');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message3', 'message.functional_case_task_delete');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message4', 'REVIEW_PASSED', '["CREATE_USER"]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_passed');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message4', 'message.functional_case_task_review');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message5', 'REVIEW_FAIL', '["CREATE_USER"]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_fail');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message5', 'message.functional_case_task_review');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message6', 'EXECUTE_PASSED', '["CREATE_USER"]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_execute_passed');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message6', 'message.functional_case_task_plan');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message7', 'EXECUTE_FAIL', '["CREATE_USER"]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_execute_fail');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message7', 'message.functional_case_task_plan');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message8', 'COMMENT', '["CREATE_USER"]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message8', 'message.functional_case_task_comment');

Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message9', 'AT', '[]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message9', 'message.functional_case_task_at_comment');

Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('case-comment_message10', 'REPLY', '[]', 'test_comment_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-case-comment-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES ('case-comment_message10', 'message.functional_case_task_reply_comment');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('xiaomeinvGTest', 1000001, 'test_guo', 'project-case-comment-test', 'test_guo', 'gyqTest', 'UN_REVIEWED', null, 'text',
        10001, '111', 'xiaomeinvGTest', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('xiaomeinvGTestOne', 1000001, 'test_guo', 'project-case-comment-test', 'test_guo', 'gyqTest1', 'UN_REVIEWED', null, 'text',
        10001, '111', 'xiaomeinvGTestOne', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null),
       ('xiaomeinvGTestTwo', 1000001, 'test_guo', 'project-case-comment-test', 'test_guo', 'gyqTest2', 'UN_REVIEWED', null, 'text',
        10001, '111', 'xiaomeinvGTestTwo', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('default-project-member-user-guo', 'default-project-member-user1', 'project-member-guo1@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-1', 'default-project-member-user2', 'project-member-guo2@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-2', 'default-project-member-user3', 'project-member-guo3@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-3', 'default-project-member-user4', 'project-member-guo4@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-4', 'default-project-member-user5', 'project-member-guo5@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-5', 'default-project-member-user6', 'project-member-guo6@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-guo-del', 'default-project-member-userDel',
        'project-member-guo-del@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000,
        NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1),
       ('gyq', 'gyq', 'gyq@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000,
        NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-guo', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'default-project-member-user-guo-1', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-2', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-3', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-4', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-5', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-del', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'gyq', 'org_member', 'organization-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-guo', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-1', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-2', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-3', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-4', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-5', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-guo-del', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test',
        UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'gyq', 'project_admin', 'project-case-comment-test', 'organization-case-comment-test', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_permission(id, role_id, permission_id)
VALUES ('user_role_guo_permission1', 'project_admin', 'FUNCTIONAL_CASE:READ+COMMENT');

INSERT INTO functional_case_comment(id, case_id, create_user, parent_id, resource_id, notifier, content, reply_user, create_time, update_time)
VALUES ('user_not_exist', 'comment_case', 'gyq', 'NONE', null, 'gyq;', '', null,UNIX_TIMESTAMP() * 1000,UNIX_TIMESTAMP() * 1000);




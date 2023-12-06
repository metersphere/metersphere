
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-review-case-test', null, 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-review-case-test', null, 'organization-review-case-test', '用例评论项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-review-case-test-1', null, 'organization-review-case-test', '用例评论项目1', '系统默认创建的项目1', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-review-case-test-2', null, 'organization-review-case-test', '用例评论项目2', '系统默认创建的项目2', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
                                                                                                                          ('project-review-case-test-3', null, 'organization-review-case-test', '用例评论项目3', '系统默认创建的项目3', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES ('test_review_case_message_robot1', 'project-review-case-test', '测试机器人1', 'IN_SITE', 'NONE', null,  null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);



Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message1', 'UPDATE', 'CREATE_USER', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message1', 'message.functional_case_task_update');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message2', 'UPDATE', 'FOLLOW_PEOPLE', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message2', 'message.functional_case_task_update');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message3', 'DELETE', 'CREATE_USER', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_delete');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message3', 'message.functional_case_task_delete');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message4', 'REVIEW_PASSED', 'CREATE_USER', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_passed');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message4', 'message.functional_case_task_review');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message5', 'REVIEW_FAIL', 'CREATE_USER', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_fail');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message5', 'message.functional_case_task_review');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message6', 'EXECUTE_PASSED', 'CREATE_USER', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_execute_passed');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message6', 'message.functional_case_task_plan');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message7', 'EXECUTE_FAIL', 'CREATE_USER', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_execute_fail');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message7', 'message.functional_case_task_plan');


Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message8', 'COMMENT', 'CREATE_USER', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message8', 'message.functional_case_task_comment');

Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message9', 'AT', 'NONE', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message9', 'message.functional_case_task_at_comment');

Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message10', 'REPLY', 'NONE', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES ('review-case_message10', 'message.functional_case_task_reply_comment');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTest', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED', null, 'text',
        10001, '111', 'gyqReviewCaseTest', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestTwo', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED', null, 'text',
        10001, '111', 'gyqReviewCaseTestTwo', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestThree', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED', null, 'text',
        10001, '111', 'gyqReviewCaseTestThree', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestFour', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED', null, 'text',
        10001, '111', 'gyqReviewCaseTestFour', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559, 1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestOne', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest1', 'UN_REVIEWED', null, 'text',
        10001, '111', 'gyqReviewCaseTestOne', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('default-project-member-user-gyq', 'default-project-member-user1', 'project-member-guo1@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-gyq-1', 'default-project-member-user2', 'project-member-guo2@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-gyq-2', 'default-project-member-user3', 'project-member-guo3@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-gyq-3', 'default-project-member-user4', 'project-member-guo4@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-gyq-4', 'default-project-member-user5', 'project-member-guo5@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-gyq-5', 'default-project-member-user6', 'project-member-guo6@metersphere.io',
        MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin',
        'admin', 0),
       ('default-project-member-user-gyq-del', 'default-project-member-userDel',
        'project-member-guo-del@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000,
        NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1),
       ('default-project-member-user-gyq-6', 'gyq', 'gyq@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000,
        NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-gyq', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'default-project-member-user-gyq-1', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-2', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-3', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-4', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-5', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-del', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'gyq', 'org_member', 'organization-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-gyq', 'project_admin', 'project-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-1', 'project_admin', 'project-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-2', 'project_admin', 'project-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-3', 'project_admin', 'project-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-4', 'project_admin', 'project-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-5', 'project_admin', 'project-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-del', 'project_admin', 'project-review-case-test', 'organization-review-case-test',
        UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'gyq', 'project_admin', 'project-review-case-test', 'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_permission(id, role_id, permission_id)
VALUES ('user_role_gyq_permission1', 'project_admin', 'FUNCTIONAL_CASE:READ+COMMENT');




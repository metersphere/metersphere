INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted,
                         delete_user, delete_time) VALUE
    ('organization-review-case-test', null, 'organization-review-case-test', 'organization-review-case-test',
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('project-review-case-test', null, 'organization-review-case-test', '用例评论项目', '系统默认创建的项目',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('project-review-case-test-1', null, 'organization-review-case-test', '用例评论项目1', '系统默认创建的项目1',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('project-review-case-test-2', null, 'organization-review-case-test', '用例评论项目2', '系统默认创建的项目2',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('project-review-case-test-3', null, 'organization-review-case-test', '用例评论项目3', '系统默认创建的项目3',
        'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user,
                          create_time, update_user, update_time, description)
VALUES ('test_review_case_message_robot1', 'project-review-case-test', '测试机器人1', 'IN_SITE', 'NONE', null, null,
        null, true, 'admin', unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000, null);



Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user,
                         create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_messageOne', 'UPDATE', '["CREATE_USER","FOLLOW_PEOPLE"]', 'test_review_case_message_robot1', 'CASE_REVIEW_TASK',
        'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000,
        true, true, 'message.title.case_review_task_update');
INSERT INTO message_task_blob(id, template)
VALUES ('review-case_messageOne', 'message.case_review_task_update');

Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user,
                         create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message3', 'DELETE', '["CREATE_USER"]', 'test_review_case_message_robot1', 'CASE_REVIEW_TASK', 'NONE',
        'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000, true,
        true, 'message.title.case_review_task_delete');
INSERT INTO message_task_blob(id, template)
VALUES ('review-case_message3', 'message.case_review_task_delete');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user,
                         create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message4', 'REVIEW_COMPLETED', '["CREATE_USER","default-project-member-user-gyq"]', 'test_review_case_message_robot1',
        'CASE_REVIEW_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',
        unix_timestamp() * 1000, true, true, 'message.title.case_review_task_review_completed');
INSERT INTO message_task_blob(id, template)
VALUES ('review-case_message4', 'message.case_review_task_review_completed');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user,
                         create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message5', 'CREATE', '["CREATE_USER","default-project-member-user-gyq"]', 'test_review_case_message_robot1', 'CASE_REVIEW_TASK', 'NONE',
        'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000, true,
        true, 'message.title.case_review_task_create');
INSERT INTO message_task_blob(id, template)
VALUES ('review-case_message5', 'message.case_review_task_create');

Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user,
                         create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message7', 'REVIEW_AT', '[]', 'test_review_case_message_robot1', 'FUNCTIONAL_CASE_TASK', 'NONE',
        'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000, true,
        true, 'message.title.functional_case_task_review_at');
INSERT INTO message_task_blob(id, template)
VALUES ('review-case_message7', 'message.functional_case_task_review_at');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user,
                         create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message8', 'REVIEW_PASSED', '["default-project-member-user-gyq-2"]', 'test_review_case_message_robot1',
        'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',
        unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_passed');
INSERT INTO message_task_blob(id, template)
VALUES ('review-case_message8', 'message.functional_case_task_review');


Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user,
                         create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES ('review-case_message9', 'REVIEW_FAIL', '["default-project-member-user-gyq-2"]', 'test_review_case_message_robot1',
        'FUNCTIONAL_CASE_TASK', 'NONE', 'project-review-case-test', true, 'admin', unix_timestamp() * 1000, 'admin',
        unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_fail');
INSERT INTO message_task_blob(id, template)
VALUES ('review-case_message9', 'message.functional_case_task_review');



INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTest', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED',
        null, 'text',
        10001, '111', 'gyqReviewCaseTest', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestTwo', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED',
        null, 'text',
        10001, '111', 'gyqReviewCaseTestTwo', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestThree', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED',
        null, 'text',
        10001, '111', 'gyqReviewCaseTestThree', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestFour', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest', 'UN_REVIEWED',
        null, 'text',
        10001, '111', 'gyqReviewCaseTestFour', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,
                            version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user,
                            update_user, delete_user, create_time, update_time, delete_time)
VALUES ('gyqReviewCaseTestOne', 1000001, 'test_guo', 'project-review-case-test', 'test_guo', 'gyqTest1', 'UN_REVIEWED',
        null, 'text',
        10001, '111', 'gyqReviewCaseTestOne', 'success', false, false, true, 'gyq', 'gyq', null, 1698058347559,
        1698058347559,
        null);

INSERT INTO user(id, name, email, password, enable, create_time, update_time, language, last_organization_id, phone,
                 source,
                 last_project_id, create_user, update_user, deleted)
VALUES ('default-project-member-user-gyq', 'default-project-member-user1', 'project-member-gyq1@metersphere.io',
        MD5('metersphere'),
        true, UNIX_TIMESTAMP() * 1000,
        UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
       ('default-project-member-user-gyq-1', 'default-project-member-user2', 'project-member-gyq2@metersphere.io',
        MD5('metersphere'), true, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL,
        'admin',
        'admin', 0),
       ('default-project-member-user-gyq-2', 'default-project-member-user3', 'project-member-gyq3@metersphere.io',
        MD5('metersphere'), true, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL,
        'admin',
        'admin', 0),
       ('default-project-member-user-gyq-3', 'default-project-member-user4', 'project-member-gyq4@metersphere.io',
        MD5('metersphere'), true, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL,
        'admin',
        'admin', 0),
       ('default-project-member-user-gyq-4', 'default-project-member-user4', 'project-member-gyq5@metersphere.io',
        MD5('metersphere'), true, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL,
        'admin',
        'admin', 0);



INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-gyq', 'org_member', 'organization-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000, 'admin'),
       (UUID(), 'default-project-member-user-gyq-1', 'org_member', 'organization-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-2', 'org_member', 'organization-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-3', 'org_member', 'organization-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-4', 'admin', 'system',
        'system', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'default-project-member-user-gyq', 'project_admin', 'project-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-1', 'project_admin', 'project-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-2', 'project_admin', 'project-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin'),
       (UUID(), 'default-project-member-user-gyq-3', 'project_admin', 'project-review-case-test',
        'organization-review-case-test', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO user_role_permission(id, role_id, permission_id)
VALUES ('user_role_gyq_permission1', 'project_admin', 'FUNCTIONAL_CASE:READ+COMMENT');




INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,
                 update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tag, platform_bug_id, deleted) VALUES
    ('bug_id_1', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'project_wx_associate_test"', 'bug-template-id', 'Local', 'open', 'default-tag', null, 0),
    ('bug_id_2', 100001, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'project_wx_associate_test"', 'bug-template-id', 'Local', 'open', 'default-tag', null, 0);

INSERT INTO bug_relation_case(id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time)
VALUES ('wx_test_id_1', 'wx_1', 'bug_id_1', 'FUNCTIONAL', null, null, 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('wx_test_id_2', 'wx_2', 'bug_id_1', 'FUNCTIONAL', 'test-plan-id', 'bug_relate_case', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('wx_test_id_3', 'wx_3', 'bug_id_2', 'FUNCTIONAL', 'test-plan-id', 'bug_relate_case-1', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
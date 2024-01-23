INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,
                 update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUES
    ('wx_bug_id_1', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'project_wx_associate_test"', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000),
    ('wx_bug_id_2', 100001, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'project_wx_associate_test"', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 10000);

INSERT INTO bug_relation_case(id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time)
VALUES ('TEST', 'wx_1', 'wx_bug_id_1', 'FUNCTIONAL', null, null, 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('wx_test_id_22', 'wx_2', 'wx_bug_id_1', 'FUNCTIONAL', 'test-plan-id', 'bug_relate_case', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('wx_test_id_33', 'wx_3', 'wx_bug_id_2', 'FUNCTIONAL', 'test-plan-id', 'bug_relate_case-1', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
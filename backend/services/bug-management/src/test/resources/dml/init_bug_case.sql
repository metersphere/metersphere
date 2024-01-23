INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,version_id, ref_id, last_execute_result,
                            deleted, public_case, latest, create_user,update_user, delete_user, create_time, update_time, delete_time)
VALUES ('bug_relate_case-tmp', 100100, 'init_module', 'default-project-for-bug', 'default_template', 'first_case', 'UN_REVIEWED', null, 'STEP',
        10001, '111', '111', 'UN_EXECUTED', 0, 0, true, 'admin', 'admin', null, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null);

INSERT INTO  project_version(id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user) VALUE
        ('default_bug_version', 'default-project-for-bug', 'v1.2.8', 'This is a test version!', 'open', true, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos,version_id, ref_id, last_execute_result,
                            deleted, public_case, latest, create_user,update_user, delete_user, create_time, update_time, delete_time) VALUES
   ('bug_relate_case', 100099, 'init_module', 'default-project-for-bug', 'default_template', 'first_case1', 'UN_REVIEWED', null, 'STEP',
        10001, 'default_bug_version', '111', 'UN_EXECUTED', 0, 0, true, 'admin', 'admin', null, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null),
   ('bug_relate_case-1', 100099, 'init_module', '100001100001', 'default_template', 'first_case2', 'UN_REVIEWED', null, 'STEP',
    10001, 'default_bug_version', '111', 'UN_EXECUTED', 0, 0, true, 'admin', 'admin', null, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null),
   ('bug_relate_case-2', 100099, 'init_module', 'default-project-for-bug', 'default_template', 'first_case2', 'UN_REVIEWED', null, 'STEP',
    10001, 'default_bug_version', '111', 'UN_EXECUTED', 0, 0, true, 'admin', 'admin', null, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null),
   ('bug_relate_case-3', 100099, 'init_module', 'default-project-for-bug', 'default_template', 'first_case2', 'UN_REVIEWED', null, 'STEP',
    10001, 'default_bug_version', '111', 'UN_EXECUTED', 0, 0, true, 'admin', 'admin', null, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null);

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, create_user, update_time, update_user)
VALUES ('init_module', 'default-project-for-bug', 'test_module_name', 'NONE', '1', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,
                 update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUES
    ('default-relate-bug-id', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000),
    ('default-relate-bug-id-1', 100001, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 10000);

INSERT INTO bug_relation_case(id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time)
VALUES ('bug-relate-case-default-id', 'bug_relate_case', 'default-relate-bug-id', 'FUNCTIONAL', 'test-plan-id', null, 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('bug-relate-case-default-id-1', 'bug_relate_case', 'default-relate-bug-id-1', 'FUNCTIONAL', 'test-plan-id', 'bug_relate_case', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('bug-relate-case-default-id-2', 'bug_relate_case-1', 'default-relate-bug-id', 'FUNCTIONAL', 'test-plan-id', 'bug_relate_case-1', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
       ('bug-relate-case-default-id-3', null, 'default-relate-bug-id', 'FUNCTIONAL', 'test-plan-id', 'bug_relate_case-3', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
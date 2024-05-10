INSERT INTO `bug_relation_case`(`id`, `case_id`, `bug_id`, `case_type`, `test_plan_id`, `test_plan_case_id`, `create_user`, `create_time`, `update_time`)
VALUES ('relate_1', 'fc_1', 'bug_1', 'FUNCTIONAL', 'test-plan-id-for-bug', 'test-plan-bug-case-id', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO `bug_relation_case`(`id`, `case_id`, `bug_id`, `case_type`, `test_plan_id`, `test_plan_case_id`, `create_user`, `create_time`, `update_time`)
VALUES ('relate_2', 'fc_2', 'bug_1', 'FUNCTIONAL', 'test-plan-id-for-bug', 'test-plan-bug-case-id', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('bug_1', 100001, 'oasis', 'admin', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('plan_1', 20000, '123', 'wx_test_plan_id_3', '1', '测试关联', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');

INSERT INTO `test_plan_functional_case`(`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`)
VALUES
    ('relate_case_1', 'plan_1', 'fc_1', 1714980158000, 'admin', NULL, NULL, NULL, 1),
    ('relate_case_2', 'plan_1', 'fc_2', 1714980158000, 'admin', NULL, NULL, NULL, 1);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES
       ('fc_1', 1, '1', '123', '100001', '111', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('fc_2', 2, '1', '123', '100001', '222', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO project_version(id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user)
values ('v3.0.0','123','v3', null, 'open', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin');


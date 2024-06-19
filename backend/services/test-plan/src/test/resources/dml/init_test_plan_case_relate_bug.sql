INSERT INTO `bug_relation_case`(`id`, `case_id`, `bug_id`, `case_type`, `test_plan_id`, `test_plan_case_id`, `create_user`, `create_time`, `update_time`)
VALUES ('relate_1', 'fc_1', 'bug_1', 'FUNCTIONAL', 'test-plan-id-for-bug', 'test-plan-bug-case-id', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO `bug_relation_case`(`id`, `case_id`, `bug_id`, `case_type`, `test_plan_id`, `test_plan_case_id`, `create_user`, `create_time`, `update_time`)
VALUES ('relate_2', 'fc_2', 'bug_1', 'FUNCTIONAL', 'test-plan-id-for-bug', 'test-plan-bug-case-id', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('bug_1', 100001, 'oasis', 'admin', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('plan_1', 20000, '123', 'wx_test_plan_id_3', 't_1', '测试关联', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('plan_2', 25000, '1234', 'wx_test_plan_id_3', 'root', '测试关联1', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('gyq_disassociate_plan_1', 25000, 'gyq_disassociate', 'wx_test_plan_id_3', 'root', '测试取消关联1', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('gyq_disassociate_plan_2', 25000, 'gyq_disassociate', 'gyq_test_plan_id_1', 'root', '测试取消关联2', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');
;

INSERT INTO `test_plan_functional_case`(`id`, `test_plan_id`, `functional_case_id`, `create_time`, `create_user`, `execute_user`, `last_exec_time`, `last_exec_result`, `pos`, `test_plan_collection_id`)
VALUES
    ('relate_case_1', 'plan_1', 'fc_1', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123'),
    ('relate_case_2', 'plan_1', 'fc_2', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123'),
    ('relate_case_3', 'plan_2', 'fc_3', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123'),
    ('gyq_disassociate_case_1', 'gyq_disassociate_plan_1', 'gyq_disassociate_fc_1', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123'),
    ('gyq_disassociate_case_2', 'gyq_disassociate_plan_1', 'gyq_disassociate_fc_2', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123'),
    ('gyq_disassociate_case_3', 'gyq_disassociate_plan_1', 'gyq_disassociate_fc_3', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123'),
    ('gyq_disassociate_case_4', 'gyq_disassociate_plan_2', 'gyq_disassociate_fc_4', 1714980158000, 'admin', NULL, NULL, NULL, 1, '123');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES
       ('fc_1', 1, 't_1', '123', '100001', '111', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('fc_2', 2, 't_1', '123', '100001', '222', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('fc_3', 3, 'root', '123', '100001', '333', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_disassociate_fc_1', 4, 'root', 'gyq_disassociate', '100001', 'disassociate1', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'gyq_disassociate_fc_1', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_disassociate_fc_2', 5, 'root', 'gyq_disassociate', '100001', 'disassociate2', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'gyq_disassociate_fc_2', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
       ('gyq_disassociate_fc_4', 5, 'root', 'gyq_disassociate', '100001', 'disassociate3', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'gyq_disassociate_fc_4', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('gyq_disassociate_fc_4', '', '', '', ' ', ' ');


    INSERT INTO project_version(id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user)
values ('v3.0.0','123','v3', null, 'open', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO project(id, num, organization_id, name, description, create_time, update_time, update_user, create_user, delete_time, deleted, delete_user, enable, module_setting)
    VALUE ('gyq_disassociate', null, 'organization_plan_associate_case_project', 'test_plan_associate_case_name', null,
           UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, false, null, true, '["bugManagement","caseManagement","apiTest","testPlan"]');


INSERT INTO `test_plan_module`(`id`, `project_id`, `name`, `parent_id`, `pos`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES
    ('t_1', '123', '计划模块', 'NONE', 256, 1714124231051, 1714124231051, '728495172886645', '728495172886645');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user)
VALUES (UUID(), 'admin', 'project_admin', 'gyq_disassociate', '100001', UNIX_TIMESTAMP() * 1000,
        'admin');

INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id)
VALUES ('100001', 'functional_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'gyq_disassociate', 0, 'FUNCTIONAL', '100001');
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id) VALUES ('bug-template-id', 'bug_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'gyq_disassociate', 0, 'BUG', 'bug-template-id');
-- 初始化项目默认模板内置字段, 项目默认模板内置字段

INSERT INTO `test_plan_collection`(`id`, `test_plan_id`, `name`, `type`, `environment_id`, `test_resource_pool_id`, `pos`, `create_user`, `create_time`, `parent_id`)
VALUES
    ('123', 'plan_1', 'coll_1', 'FUNCTIONAL', 'NONE', 'NONE', 1, 'admin', 1716370415311, '123456');

INSERT INTO `test_plan_case_execute_history`(`id`, `test_plan_case_id`, `test_plan_id`, `case_id`, `status`, `content`, `steps`, `deleted`, `notifier`, `create_user`, `create_time`)
VALUES
    ('123445', 'relate_case_1', 'plan_1', 'fc_1', 'PASSED', '1234', '2132134', b'0', '', 'admin', 1715828421525);
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('fc_1', 'STEP', '1111', '', '', 'TEST');

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting)
VALUES
    ('123', 2, 1, 'wx', 'wx', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');
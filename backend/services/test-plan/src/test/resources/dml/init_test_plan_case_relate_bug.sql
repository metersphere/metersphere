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
       ('fc_2', 2, 'root', '123', '100001', '222', 'UN_REVIEWED', NULL, 'TEXT', 55000, 'v3.0.0', 'TEST_FUNCTIONAL_MINDER_CASE_ID_7', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
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
    ('123', 'plan_1', 'coll_1', 'FUNCTIONAL', 'NONE', 'NONE', 1, 'admin', 1716370415311, '123456'),
    ('223', 'plan_1', 'api_coll_1', 'API', 'NONE', 'NONE', 2, 'admin', 1716370415311, '123456'),
    ('323', 'plan_1', 'scenario_coll_1', 'SCENARIO', 'NONE', 'NONE', 3, 'admin', 1716370415311, '123456');

INSERT INTO `test_plan_case_execute_history`(`id`, `test_plan_case_id`, `test_plan_id`, `case_id`, `status`, `content`, `steps`, `deleted`, `notifier`, `create_user`, `create_time`)
VALUES
    ('123445', 'relate_case_1', 'plan_1', 'fc_1', 'PASSED', '1234', null, b'0', '', 'admin', 1715828421525);
INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description) VALUES ('fc_1', 'STEP', '1111', '', '', 'TEST');

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting)
VALUES
    ('123', 2, 1, 'wx', 'wx', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');

INSERT INTO `test_plan_config`(`test_plan_id`, `automatic_status_update`, `repeat_case`, `pass_threshold`, `case_run_mode`)
VALUES ('plan_1', b'0', b'0', 100.00, 'PARALLEL');


INSERT INTO project_application (project_id, type, type_value) VALUES
    ('123', 'BUG_DEFAULT_TEMPLATE', 'default-bug-template-id'),
    ('123', 'BUG_SYNC_BUG_PLATFORM_CONFIG', '{"jiraKey":"TES","jiraBugTypeId":"10009"}'),
    ('123', 'BUG_SYNC_CRON_EXPRESSION', '0 0 0/1 * * ?'),
    ('123', 'BUG_SYNC_MECHANISM', 'increment'),
    ('123', 'BUG_SYNC_PLATFORM_KEY', 'jira'),
    ('123-integration', 'BUG_SYNC_PLATFORM_KEY', 'jira'),
    ('123', 'BUG_SYNC_SYNC_ENABLE', 'true'),
    ('123', 'CASE_RELATED_CASE_ENABLE', 'false'),
    ('123', 'CASE_RELATED_DEMAND_PLATFORM_CONFIG', '{"jiraKey":"TES","jiraDemandTypeId":"10007"}'),
    ('123', 'CASE_RELATED_PLATFORM_KEY', 'jira');


INSERT INTO service_integration(`id`, `plugin_id`, `enable`, `configuration`, `organization_id`) VALUES
    ('621103810617343', 'jira', true, 0x504B0304140008080800BC517657000000000000000000000000030000007A6970258DC10EC2201044FF65CF06D2C498D89347B5574FBD6D8158222CD85D6268E3BF4BE3F5CDBC990DD0DAC531430FB348E65EEBE06B41AAA9289480CC1E4991130D07C022F3A366D7DA13B2373B32261592469AF1572FCF883E289362CB735BF8A4C5EE073474C3CB8E59A6F85EEFF12AE676EC4E67F8FE00504B0708384DA4307800000087000000504B01021400140008080800BC517657384DA43078000000870000000300000000000000000000000000000000007A6970504B0506000000000100010031000000A90000000000, '1'),
    ('652096294625284', 'zentao', true, 0x504B030414000808080093756458000000000000000000000000030000007A6970AB564A4C49294A2D2E56B252CA282929B0D2D7373437D23334D3333230D033B3B4B230B0B050D2514A4C4ECE2FCD2B01AA4A4CC9CDCC038A1424161797E717A500859C1373F2F3D21D8C0C0C4D811245A985A5A9C525219505A940B900C7108F784F3F377FA55A00504B07088A813510680000006C000000504B01021400140008080800937564588A813510680000006C0000000300000000000000000000000000000000007A6970504B0506000000000100010031000000990000000000, '1');

INSERT INTO bug_relation_case (id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time) VALUES
    ('bug_relate_1', 'fc_1', 'bug_1', 'FUNCTIONAL', 'plan_1', 'relate_case_1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bug_relate_2', 'fc_1', 'bug_2', 'FUNCTIONAL', 'plan_1', 'relate_case_1', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, create_user, update_time, update_user)
VALUES
    ('t_1', '123', '测试模块', 'NONE', '1', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin'),
    ('t_1_1', '123', '测试模块_1', 't_1', '2', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin');

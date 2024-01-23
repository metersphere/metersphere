-- 模拟数据
INSERT INTO `service_integration`(`id`, `plugin_id`, `enable`, `configuration`, `organization_id`) VALUES ('1', 'jira', b'1', 0x504B0304140008080800BC517657000000000000000000000000030000007A6970258DC10EC2201044FF65CF06D2C498D89347B5574FBD6D8158222CD85D6268E3BF4BE3F5CDBC990DD0DAC531430FB348E65EEBE06B41AAA9289480CC1E4991130D07C022F3A366D7DA13B2373B32261592469AF1572FCF883E289362CB735BF8A4C5EE073474C3CB8E59A6F85EEFF12AE676EC4E67F8FE00504B0708384DA4307800000087000000504B01021400140008080800BC517657384DA43078000000870000000300000000000000000000000000000000007A6970504B0506000000000100010031000000A90000000000, '100002');


-- 模拟用户
replace INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user) VALUE
    ('wx-test', 'wx-test-1', 'wx-test-user@metersphere.io', MD5('metersphere'),
     UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');

replace INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES (UUID_SHORT(), 'wx-test', 'project_admin', '100001100001', '100001', 1684747668375, 'admin');

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-application', null, '100002', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
    ('project_application_test_id', null, '100003', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 集成信息
INSERT INTO project_application (project_id, type, type_value) VALUES
                                                                   ('default-project-for-application', 'BUG_DEFAULT_TEMPLATE', 'default-bug-template-id'),
                                                                   ('default-project-for-application', 'BUG_SYNC_BUG_PLATFORM_CONFIG', '{"jiraKey":"TES","jiraBugTypeId":"10009"}'),
                                                                   ('default-project-for-application', 'BUG_SYNC_CRON_EXPRESSION', '0 0 0/1 * * ?'),
                                                                   ('default-project-for-application', 'BUG_SYNC_MECHANISM', 'increment'),
                                                                   ('default-project-for-application', 'BUG_SYNC_PLATFORM_KEY', 'jira'),
                                                                   ('default-project-for-application', 'BUG_SYNC_SYNC_ENABLE', 'true'),
                                                                   ('default-project-for-application', 'CASE_RELATED_CASE_ENABLE', 'false'),
                                                                   ('default-project-for-application', 'CASE_RELATED_DEMAND_PLATFORM_CONFIG', '{"jiraKey":"TES","jiraDemandTypeId":"10007"}'),
                                                                   ('default-project-for-application', 'CASE_RELATED_PLATFORM_KEY', 'jira');


INSERT INTO service_integration(`id`, `plugin_id`, `enable`, `configuration`, `organization_id`) VALUES
    ('621103810617345', 'jira', true, 0x504B0304140008080800BC517657000000000000000000000000030000007A6970258DC10EC2201044FF65CF06D2C498D89347B5574FBD6D8158222CD85D6268E3BF4BE3F5CDBC990DD0DAC531430FB348E65EEBE06B41AAA9289480CC1E4991130D07C022F3A366D7DA13B2373B32261592469AF1572FCF883E289362CB735BF8A4C5EE073474C3CB8E59A6F85EEFF12AE676EC4E67F8FE00504B0708384DA4307800000087000000504B01021400140008080800BC517657384DA43078000000870000000300000000000000000000000000000000007A6970504B0506000000000100010031000000A90000000000, '100002');


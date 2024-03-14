INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-bug-tmp', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-bug', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
    ('no-status-project', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
    ('default-project-for-bug-no-local', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES
    (UUID(), 'admin', 'project_admin', 'default-project-for-bug', '100001', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time, update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUES
    ('default-bug-id', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000),
    ('default-bug-id-tapd1', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'default-bug-template-id', 'Tapd', 'open', '["default-tag"]', null, 0, 10000),
    ('default-bug-id-tapd2', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug-no-local', 'default-bug-template-id', 'Tapd', 'open', '["default-tag"]', null, 0, 5000),
    ('default-bug-id-single', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug-single', 'default-bug-template-id', 'Tapd', 'open', '["default-tag"]', null, 0, 5000),
    ('default-bug-id-jira', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug-single', 'default-bug-template-id', 'Jira', 'open', '["default-tag"]', 'TES-TEST', 0, 10000),
    ('default-bug-id-jira-sync', 100000, '这是一段summary444', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'jira', 'Jira', 'open', '["default-tag"]', 'TES-TEST', 0, 15000),
    ('default-bug-id-jira-sync-1', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'jira-test', 'Jira', 'open', '["default-tag"]', 'TES-TEST', 0, 20000);

INSERT INTO bug_content(bug_id, description) VALUE ('default-bug-id', 'oasis');
INSERT INTO bug_custom_field (bug_id, field_id, value) VALUE ('default-bug-id', 'test_field', '["default", "default-1"]');
INSERT INTO bug_custom_field (bug_id, field_id, value) VALUE ('default-bug-id-jira-sync', 'summary', '这是一段summary444');

INSERT INTO custom_field (id, name, scene, type, remark, internal, scope_type, create_time, update_time, create_user, scope_id) VALUE
    ('test_field', '测试字段', 'BUG', 'MULTIPLE_SELECT', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-for-bug'),
    ('custom-field-1', '测试字段1', 'BUG', 'SELECT', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-for-bug'),
    ('custom-field-2', '测试字段2', 'BUG', 'SELECT', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-for-bug'),
    ('custom-field-3', '测试字段3', 'BUG', 'MEMBER', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-for-bug'),
    ('custom-field-4', '测试字段4', 'BUG', 'MULTIPLE_MEMBER', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-for-bug');

INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, scene) VALUES
    ('bug-template-id', 'bug-template', '', 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'default-project-for-bug', 0, 'BUG'),
    ('default-bug-template-id', 'bug-default-template', '', 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'default-project-for-bug', 0, 'BUG'),
    ('no-status-template', 'no-status-template', '', 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'no-status-project', 0, 'BUG');
INSERT INTO template_custom_field(`id`, `field_id`, `template_id`, `required`, `system_field`, `pos`, `api_field_id`, `default_value`) VALUES
    ('100581234408685570', 'custom-field-1', 'default-bug-template-id', false, false, 0, 'customfield_10097', NULL),
    ('100581234408685571', 'custom-field-2', 'default-bug-template-id', false, false, 0, 'customfield_10098', NULL),
    ('100581234408685572', 'custom-field-3', 'default-bug-template-id', false, false, 0, '', NULL),
    ('100581234408685573', 'custom-field-4', 'default-bug-template-id', false, false, 0, '', NULL);

INSERT INTO bug_relation_case (id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time) VALUE
    (UUID_SHORT(), UUID_SHORT(), 'default-bug-id', 'functional', null, null, 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO file_metadata (id, name, type, size, create_time, update_time, project_id, storage, create_user, update_user,
                           tags, description, module_id, path, latest, ref_id, file_version) VALUES
    ('default-bug-file-id-1', 'test-file', 'xlsx', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'MINIO', 'admin', 'admin',
     '["default-tag"]', 'test-file', null, 'test-file', 1, 'default-bug-id', 1),
    ('default-bug-file-id-2', 'test-file', 'xlsx', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'MINIO', 'admin', 'admin',
    '["default-tag"]', 'test-file', null, 'test-file', 1, 'default-bug-id', 1);

INSERT INTO bug_local_attachment (id, bug_id, file_id, file_name, size, create_user, create_time) VALUES
  ('default-local-attachment-id', 'default-bug-id', 'default-bug-file-id', 'test-file-1', 100, 'admin', UNIX_TIMESTAMP() * 1000);

INSERT INTO file_association (id, source_type, source_id, file_id, file_ref_id, file_version, create_user, create_time, update_user, update_time) VALUES
  ('default-file-association-id', 'BUG', 'default-bug-id', 'default-bug-file-id-1', 'default-bug-file-id-1', '1',  'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000);

INSERT INTO project_application (project_id, type, type_value) VALUES
       ('default-project-for-bug', 'BUG_DEFAULT_TEMPLATE', 'default-bug-template-id'),
       ('default-project-for-bug', 'BUG_SYNC_BUG_PLATFORM_CONFIG', '{"jiraKey":"TES","jiraBugTypeId":"10009"}'),
       ('default-project-for-bug', 'BUG_SYNC_CRON_EXPRESSION', '0 0 0/1 * * ?'),
       ('default-project-for-bug', 'BUG_SYNC_MECHANISM', 'increment'),
       ('default-project-for-bug', 'BUG_SYNC_PLATFORM_KEY', 'jira'),
       ('default-project-for-not-integration', 'BUG_SYNC_PLATFORM_KEY', 'jira'),
       ('default-project-for-bug', 'BUG_SYNC_SYNC_ENABLE', 'true'),
       ('default-project-for-bug', 'CASE_RELATED_CASE_ENABLE', 'false'),
       ('default-project-for-bug', 'CASE_RELATED_DEMAND_PLATFORM_CONFIG', '{"jiraKey":"TES","jiraDemandTypeId":"10007"}'),
       ('default-project-for-bug', 'CASE_RELATED_PLATFORM_KEY', 'jira');


INSERT INTO service_integration(`id`, `plugin_id`, `enable`, `configuration`, `organization_id`) VALUES
    ('621103810617344', 'jira', true, 0x504B0304140008080800BC517657000000000000000000000000030000007A6970258DC10EC2201044FF65CF06D2C498D89347B5574FBD6D8158222CD85D6268E3BF4BE3F5CDBC990DD0DAC531430FB348E65EEBE06B41AAA9289480CC1E4991130D07C022F3A366D7DA13B2373B32261592469AF1572FCF883E289362CB735BF8A4C5EE073474C3CB8E59A6F85EEFF12AE676EC4E67F8FE00504B0708384DA4307800000087000000504B01021400140008080800BC517657384DA43078000000870000000300000000000000000000000000000000007A6970504B0506000000000100010031000000A90000000000, '100001'),
    ('652096294625281', 'zentao', true, 0x504B030414000808080093756458000000000000000000000000030000007A6970AB564A4C49294A2D2E56B252CA282929B0D2D7373437D23334D3333230D033B3B4B230B0B050D2514A4C4ECE2FCD2B01AA4A4CC9CDCC038A1424161797E717A500859C1373F2F3D21D8C0C0C4D811245A985A5A9C525219505A940B900C7108F784F3F377FA55A00504B07088A813510680000006C000000504B01021400140008080800937564588A813510680000006C0000000300000000000000000000000000000000007A6970504B0506000000000100010031000000990000000000, '100001');





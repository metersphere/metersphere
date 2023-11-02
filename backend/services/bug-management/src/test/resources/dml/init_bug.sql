INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-bug-tmp', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-bug', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,
                 update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tag, platform_bug_id, trash) VALUES
    ('default-bug-id', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'bug-template-id', 'Local', 'open', 'default-tag', null, 0),
    ('default-bug-id-tapd1', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'default-bug-template-id', 'Tapd', 'open', 'default-tag', null, 0),
    ('default-bug-id-tapd2', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug-no-local', 'default-bug-template-id', 'Tapd', 'open', 'default-tag', null, 0),
    ('default-bug-id-single', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-bug-single', 'default-bug-template-id', 'Tapd', 'open', 'default-tag', null, 0);

INSERT INTO bug_custom_field (bug_id, field_id, value) VALUE ('default-bug-id', 'test_field', '["default", "default-1"]');

INSERT INTO custom_field (id, name, scene, type, remark, internal, scope_type, create_time, update_time, create_user, scope_id) VALUE
    ('test_field', '测试字段', 'BUG', 'MULTIPLE_SELECT', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-for-bug'),
    ('custom-field', '测试字段', 'BUG', 'SELECT', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'default-project-for-bug');

INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, scene) VALUES
    ('bug-template-id', 'bug-template', '', 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'default-project-for-bug', 0, 'BUG'),
    ('default-bug-template-id', 'bug-default-template', '', 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', 'default-project-for-bug', 0, 'BUG');

INSERT INTO bug_relation_case (id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time) VALUE
    (UUID_SHORT(), UUID_SHORT(), 'default-bug-id', 'functional', null, null, 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO file_metadata (id, name, type, size, create_time, update_time, project_id, storage, create_user, update_user,
                           tags, description, module_id, path, latest, ref_id, file_version) VALUES
    ('default-bug-file-id-1', 'test-file', 'xlsx', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'MINIO', 'admin', 'admin',
     '["default-tag"]', 'test-file', null, 'test-file', 1, 'default-bug-id', 1),
    ('default-bug-file-id-2', 'test-file', 'xlsx', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'default-project-for-bug', 'MINIO', 'admin', 'admin',
    '["default-tag"]', 'test-file', null, 'test-file', 1, 'default-bug-id', 1);

INSERT INTO project_application (project_id, type, type_value) VALUES ('default-project-for-bug', 'BUG_DEFAULT_TEMPLATE', 'default-bug-template-id');
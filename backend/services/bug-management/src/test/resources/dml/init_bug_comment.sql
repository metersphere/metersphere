INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-bug-comment', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user, deleted) VALUES
    ('oasis-user-id', 'oasis', 'oasis@test.com', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
    ('oasis-user-id1', 'oasis1', 'oasis1@test.com', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
    ('oasis-user-id2', 'oasis2', 'oasis2@test.com', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
    ('oasis-user-id3', 'oasis3', 'oasis3@test.com', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false),
    ('oasis-user-id4', 'oasis4', 'oasis4@test.com', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,
                 update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUE
    ('default-bug-id-for-comment', 100099, 'default-bug-for-comment', 'oasis', 'oasis', 'oasis-user-id', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000),
    ('default-bug-id-for-comment1', 100099, 'default-bug-for-comment', 'oasis', 'oasis', 'oasis-user-id', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 10000);

INSERT INTO bug_comment (id, bug_id, reply_user, notifier, parent_id, content, create_user, create_time, update_user, update_time) VALUES
    ('default-bug-comment-id-1', 'default-bug-id-for-comment', null, null, null, 'This is a test comment!', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000),
    ('default-bug-comment-id-2', 'default-bug-id-for-comment', 'admin', 'oasis-user-id1;oasis-user-id2', 'default-bug-comment-id-1', 'This is a test comment!', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000),
    ('default-bug-comment-id-3', 'default-bug-id-for-comment', null, null, null, 'This is a test comment!', 'admin', UNIX_TIMESTAMP() * 1000 + 1000, 'admin', UNIX_TIMESTAMP() * 1000),
    ('default-bug-comment-id-4', 'default-bug-id-for-comment', 'oasis-user-id1', null, 'default-bug-comment-id-3', 'This is a test comment!', 'admin', UNIX_TIMESTAMP() * 1000 + 1000, 'admin', UNIX_TIMESTAMP() * 1000),
    ('default-bug-comment-id-5', 'default-bug-id-for-comment', 'oasis-user-id1', null, null, 'This is a test comment!', 'oasis', UNIX_TIMESTAMP() * 1000 - 1000, 'admin', UNIX_TIMESTAMP() * 1000);

INSERT INTO bug_custom_field (bug_id, field_id, value) VALUE ('default-bug-id-for-comment1', 'comment_test_field', '["default", "default-1"]');

INSERT INTO custom_field (id, name, scene, type, remark, internal, scope_type, create_time, update_time, create_user, scope_id) VALUE
    ('comment_test_field', '测试字段', 'BUG', 'MULTIPLE_SELECT', '', 0, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001100001');

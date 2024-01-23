INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-clean-resource-tmp', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time, update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUE
    ('bug-clean-resource-id', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-clean-resource', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

INSERT INTO bug_content (bug_id, description) VALUE ('bug-clean-resource-id', 'default-bug-description');

INSERT INTO bug_follower (bug_id, user_id) VALUE ('bug-clean-resource-id', 'admin');

INSERT INTO bug_local_attachment (id, bug_id, file_id, file_name, size, source, create_user, create_time) VALUE
    ('bug-clean-resource-attachment-id', 'bug-clean-resource-id', 'file-for-sync-extra', 'sync-extra-file-local-A.txt', 100, 'ATTACHMENT', 'admin', UNIX_TIMESTAMP() * 1000);

INSERT INTO bug_comment(id, bug_id, content, create_user, create_time, update_user, update_time) VALUE
    ('bug-clean-resource-comment-id', 'bug-clean-resource-id', 'This is a comment!', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000);

INSERT INTO bug_custom_field(bug_id, field_id, value) VALUE ('bug-clean-resource-id', 'custom-field-id', 'custom-field-value');

INSERT INTO bug_relation_case (id, case_id, bug_id, case_type, test_plan_id, test_plan_case_id, create_user, create_time, update_time) VALUE
    ('bug-clean-resource-relation-id', 'case-id', 'bug-clean-resource-id', 'case', 'test-plan-id', 'test-plan-case-id', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
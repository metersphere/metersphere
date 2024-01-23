INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-bug-tmp', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('project-for-sync-extra', null, '100001', '测试项目(缺陷同步)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time, update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUE
    ('bug-for-sync-extra', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'project-for-sync-extra', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 0, 5000);

INSERT INTO file_metadata (id, name, type, size, create_time, update_time, project_id, storage, create_user, update_user,
                           tags, description, module_id, path, latest, ref_id, file_version) VALUE
    ('file-for-sync-extra', 'sync-extra-file-associate-A', 'xlsx', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'project-for-sync-extra', 'MINIO', 'admin', 'admin',
     '["default-tag"]', 'test-file', null, 'test-file', 1, 'default-bug-id', 1);

INSERT INTO file_association(id, source_type, source_id, file_id, file_ref_id, file_version, create_time, update_user, update_time, create_user) VALUE
('association-for-sync-extra', 'bug', 'bug-for-sync-extra', 'file-for-sync-extra', 'default-bug-id', 1, UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO bug_local_attachment (id, bug_id, file_id, file_name, size, source, create_user, create_time) VALUE
('bug-local-attachment-for-sync-extra', 'bug-for-sync-extra', 'file-for-sync-extra', 'sync-extra-file-local-A.txt', 100, 'ATTACHMENT', 'admin', UNIX_TIMESTAMP() * 1000),
('bug-local-attachment-for-sync-extra-null', 'bug-for-sync-extra', 'file-for-sync-extra-null', '', 100, 'ATTACHMENT', 'admin', UNIX_TIMESTAMP() * 1000);
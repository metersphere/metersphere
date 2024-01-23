INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
    VALUE ('default-project-for-bug-tmp-1', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
    VALUE ('default-project-for-attachment', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time, update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos)
VALUES ('default-attachment-bug-id', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-attachment', 'bug-template-id', 'Local', 'open', '["default-tag"]', null, 1, 5000),
       ('default-bug-id-tapd', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'default-project-for-attachment', 'default-bug-template-id', 'Tapd', 'open', '["default-tag"]', null, 0, 10000);
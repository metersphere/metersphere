INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('bug-his-project-tmp', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO bug (id, num, title, handle_users, handle_user, create_user, create_time,
                 update_user, update_time, delete_user, delete_time, project_id, template_id, platform, status, tags, platform_bug_id, deleted, pos) VALUES
    ('bug-history-id', 100000, 'default-bug', 'oasis', 'oasis', 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, '100001100001', 'bug-template-id', 'Local', 'open', null, null, 1, 5000);

INSERT INTO operation_history (`id`, `project_id`, `create_time`, `create_user`, `source_id`, `type`, `module`, `ref_id`) VALUES
      (1, '100001100001', 1706079964322, 'admin', 'bug-history-id', 'ADD', 'BUG_MANAGEMENT_INDEX', NULL),
      (2, '100001100001', 1706079964322, 'admin', 'bug-history-id', 'ADD', 'BUG_MANAGEMENT_RECYCLE', NULL);
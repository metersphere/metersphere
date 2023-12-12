INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-status-tmp', null, '100001', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUE
    ('default-project-for-status', null, '100003', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
    ('default-project-for-no-status', null, '100003', '测试项目(缺陷)', '系统默认创建的项目(缺陷)', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

INSERT INTO status_item(`id`, `name`, `scene`, `remark`, `internal`, `scope_type`, `ref_id`, `scope_id`, `pos`) VALUES ('1', '新建', 'BUG', NULL, b'0', '0', NULL, 'default-project-for-status', 0);
INSERT INTO status_item(`id`, `name`, `scene`, `remark`, `internal`, `scope_type`, `ref_id`, `scope_id`, `pos`) VALUES ('2', '处理中', 'BUG', NULL, b'0', '0', NULL, 'default-project-for-status', 0);
INSERT INTO status_definition(`status_id`, `definition_id`) VALUES ('1', 'START');
INSERT INTO status_flow(`id`, `from_id`, `to_id`) VALUES ('1', '1', '2');

ALTER TABLE mock_expect_config ADD COLUMN expect_num varchar(50);

insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+UPLOAD_JAR', 'WORKSPACE_PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_member', 'WORKSPACE_PROJECT_MANAGER:READ+UPLOAD_JAR', 'WORKSPACE_PROJECT_MANAGER');
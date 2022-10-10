-- ws admin
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+CREATE_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+EDIT_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+COPY_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+DELETE_GROUP', 'WORKSPACE_PROJECT_ENVIRONMENT');
-- delete old permission
delete
from user_group_permission
where permission_id = 'WORKSPACE_TEMPLATE:READ+REPORT_TEMPLATE';
-- admin
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'admin', 'SYSTEM_PLUGIN:READ', 'SYSTEM_PLUGIN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'admin', 'SYSTEM_PLUGIN:UPLOAD', 'SYSTEM_PLUGIN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'admin', 'SYSTEM_PLUGIN:DEL', 'SYSTEM_PLUGIN');


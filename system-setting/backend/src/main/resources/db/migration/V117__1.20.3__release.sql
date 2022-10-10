-- V117_v1-20-3_ui-element-permission
-- start

-- 项目管理员
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_UI_ELEMENT:READ+IMPORT', 'PROJECT_UI_ELEMENT');

insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_UI_ELEMENT:READ+EXPORT', 'PROJECT_UI_ELEMENT');


-- 项目成员
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_UI_ELEMENT:READ+IMPORT', 'PROJECT_UI_ELEMENT');

insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_UI_ELEMENT:READ+EXPORT', 'PROJECT_UI_ELEMENT');
-- end

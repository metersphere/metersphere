-- 更新资源池
UPDATE test_resource_pool
SET backend_listener = 1;

# permission
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+RECOVER', 'PROJECT_TRACK_CASE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_API_DEFINITION:READ+TIMING_SYNC', 'PROJECT_API_DEFINITION');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_PERFORMANCE_REPORT:READ+COMPARE', 'PROJECT_PERFORMANCE_REPORT');

insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+RECOVER', 'PROJECT_TRACK_CASE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_API_DEFINITION:READ+TIMING_SYNC', 'PROJECT_API_DEFINITION');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_PERFORMANCE_REPORT:READ+COMPARE', 'PROJECT_PERFORMANCE_REPORT');

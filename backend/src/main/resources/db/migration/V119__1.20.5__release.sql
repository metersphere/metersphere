-- 初始化 sql
-- start V119_1-20-5_ws_permission
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+EDIT_USER', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+ADD_USER', 'WORKSPACE_PROJECT_MANAGER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+DELETE_USER', 'WORKSPACE_PROJECT_MANAGER');
-- end

-- project_admin
-- start V119-1_20_5_add_permission
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_EDIT', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_MOVE', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_COPY', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_DELETE', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_REDUCTION', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+GENERATE_DEPENDENCIES', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_ADD_PUBLIC', 'PROJECT_TRACK_CASE');


-- project_member
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_EDIT', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_MOVE', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_COPY', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_DELETE', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_REDUCTION', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+GENERATE_DEPENDENCIES', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_ADD_PUBLIC', 'PROJECT_TRACK_CASE');
-- end
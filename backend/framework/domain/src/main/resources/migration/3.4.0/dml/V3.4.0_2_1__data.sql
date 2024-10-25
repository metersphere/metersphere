-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 定时任务历史数据num值处理
UPDATE `schedule` set num = UUID_SHORT() % 10000000;

-- 定时任务历史数据name处理
update `schedule` set name = '定时同步缺陷' where name = 'Bug Sync Job';
update `schedule` set name = '定时同步需求' where name = 'Demand Sync Job';


-- 项目下所有用户组默认添加任务中心全部权限
-- 用例任务查询
INSERT INTO user_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), u.id, p.permission_id
FROM user_role u
         JOIN (SELECT 'PROJECT_CASE_TASK_CENTER:READ' AS permission_id
               UNION ALL
               SELECT 'PROJECT_CASE_TASK_CENTER:EXEC+STOP'
               UNION ALL
               SELECT 'PROJECT_CASE_TASK_CENTER:READ+DELETE'
               UNION ALL
               SELECT 'PROJECT_SCHEDULE_TASK_CENTER:READ'
               UNION ALL
               SELECT 'PROJECT_SCHEDULE_TASK_CENTER:READ+UPDATE'
               UNION ALL
               SELECT 'PROJECT_SCHEDULE_TASK_CENTER:READ+DELETE') p
WHERE u.type = 'PROJECT';


-- 组织管理员默认添加任务中心全部权限
INSERT INTO user_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), u.id, p.permission_id
FROM user_role u
         JOIN (SELECT 'ORGANIZATION_CASE_TASK_CENTER:READ' AS permission_id
               UNION ALL
               SELECT 'ORGANIZATION_CASE_TASK_CENTER:EXEC+STOP'
               UNION ALL
               SELECT 'ORGANIZATION_CASE_TASK_CENTER:READ+DELETE'
               UNION ALL
               SELECT 'ORGANIZATION_SCHEDULE_TASK_CENTER:READ'
               UNION ALL
               SELECT 'ORGANIZATION_SCHEDULE_TASK_CENTER:READ+UPDATE'
               UNION ALL
               SELECT 'ORGANIZATION_SCHEDULE_TASK_CENTER:READ+DELETE') p
WHERE u.id = 'org_admin';

-- 历史用户组中包含了原任务中心查询权限的用户组，选中用例执行任务-查询和系统后台任务-查询
INSERT INTO user_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), urp.role_id, p.permission_id
FROM user_role_permission urp
         JOIN (SELECT 'ORGANIZATION_CASE_TASK_CENTER:READ' AS permission_id
               UNION ALL
               SELECT 'ORGANIZATION_SCHEDULE_TASK_CENTER:READ') p
         INNER JOIN user_role on user_role.id = urp.role_id
WHERE urp.permission_id = 'ORGANIZATION_TASK_CENTER:READ'
  and user_role.type = 'ORGANIZATION'
  and user_role.id != 'org_admin';


-- 历史用户组中包含了停止权限的用户组，选中用例执行任务-执行停止和系统后台任务-编辑
INSERT INTO user_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), urp.role_id, p.permission_id
FROM user_role_permission urp
         JOIN (SELECT 'ORGANIZATION_CASE_TASK_CENTER:EXEC+STOP' AS permission_id
               UNION ALL
               SELECT 'ORGANIZATION_SCHEDULE_TASK_CENTER:READ+UPDATE') p
         INNER JOIN user_role on user_role.id = urp.role_id
WHERE urp.permission_id = 'ORGANIZATION_TASK_CENTER:READ+STOP'
  and user_role.type = 'ORGANIZATION'
  and user_role.id != 'org_admin';


-- 系统任务中心权限
-- 历史用户组中包含了原任务中心查询权限的用户组，选中用例执行任务-查询和系统后台任务-查询
INSERT INTO user_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), urp.role_id, p.permission_id
FROM user_role_permission urp
         JOIN (SELECT 'SYSTEM_CASE_TASK_CENTER:READ' AS permission_id
               UNION ALL
               SELECT 'SYSTEM_SCHEDULE_TASK_CENTER:READ') p
         INNER JOIN user_role on user_role.id = urp.role_id
WHERE urp.permission_id = 'SYSTEM_TASK_CENTER:READ'
  and user_role.type = 'SYSTEM'
  and user_role.id != 'admin';


-- 历史用户组中包含了停止权限的用户组，选中用例执行任务-执行停止和系统后台任务-编辑
INSERT INTO user_role_permission (id, role_id, permission_id)
SELECT UUID_SHORT(), urp.role_id, p.permission_id
FROM user_role_permission urp
         JOIN (SELECT 'SYSTEM_CASE_TASK_CENTER:EXEC+STOP' AS permission_id
               UNION ALL
               SELECT 'SYSTEM_SCHEDULE_TASK_CENTER:READ+UPDATE') p
         INNER JOIN user_role on user_role.id = urp.role_id
WHERE urp.permission_id = 'SYSTEM_TASK_CENTER:READ+STOP'
  and user_role.type = 'SYSTEM'
  and user_role.id != 'admin';

-- 初始化项目管理员定义分享权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ+SHARE');
-- 初始化项目管理员任务中心权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_TASK:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_TASK:UPDATE');

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
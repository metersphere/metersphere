-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化组织
INSERT INTO organization (id, num, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), 100001, '默认组织', '系统默认创建的组织', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
-- 初始化项目
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), 100001, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);

-- 初始化用户
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user,deleted)
VALUES ('admin', 'Administrator', 'admin@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin',false);

-- 初始化用户组
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('admin', '系统管理员', '拥有系统全部组织以及项目的操作权限', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'GLOBAL');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('member', '系统成员', '系统内初始化的用户', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'GLOBAL');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_admin', '组织管理员', '组织管理员', 1, 'ORGANIZATION', 1620674220007, 1620674220000, 'admin', 'GLOBAL');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_member', '组织成员', '组织成员', 1, 'ORGANIZATION', 1620674220008, 1620674220000, 'admin', 'GLOBAL');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_admin', '项目管理员', '项目管理员', 1, 'PROJECT', 1620674220004, 1620674220000, 'admin', 'GLOBAL');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_member', '项目成员', '项目成员', 1, 'PROJECT', 1620674220005, 1620674220000, 'admin', 'GLOBAL');

-- 初始化用户和组的关系
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user) VALUES (uuid(), 'admin', 'admin', 'SYSTEM', 1684747668375, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user) VALUES (uuid(), 'admin', 'member', 'SYSTEM', 1684747668375, 'admin');


-- 初始化用户组权限
-- 系统管理员拥有所有的权限，不用初始化

-- 组织管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_PROJECT:READ+DELETE_USER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_PROJECT:READ+ADD_USER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_PROJECT:READ+UPDATE_USER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_QUOTA:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_QUOTA:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'PERSONAL_INFORMATION:READ+UPDATE_PASSWORD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'PERSONAL_INFORMATION:READ+API_KEYS');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'PERSONAL_INFORMATION:READ+THIRD_ACCOUNT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'PERSONAL_INFORMATION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+DELETE_GROUP');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+COPY_GROUP');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+UPDATE_GROUP');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+CREATE_GROUP');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+UPLOAD_JAR');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_MESSAGE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_MESSAGE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_SERVICE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_SERVICE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_OPERATING_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+COPY');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_PROJECT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_PROJECT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_PROJECT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+CASE_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+ISSUE_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+CUSTOM');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ+ADD');
-- 组织成员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_PROJECT_MANAGER:READ+UPLOAD_JAR');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_MESSAGE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_SERVICE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_OPERATING_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'SYSTEM_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_TEMPLATE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_USER:READ');

-- 项目管理员权限

-- 项目成员权限

-- 只读用户的权限

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化组织
INSERT INTO organization (id, num, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), 100001, '默认组织', '系统默认创建的组织', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
-- 初始化项目
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES (uuid(), 100001, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);

-- 初始化用户
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user)
VALUES ('admin', 'Administrator', 'admin@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');

-- 初始化用户组
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('admin', '系统管理员(系统)', '拥有系统全部组织以及项目的操作权限', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('member', '系统成员', '系统内初始化的用户', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_admin', '组织管理员(系统)', '组织管理员', 1, 'ORGANIZATION', 1620674220007, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_member', '组织成员(系统)', '组织成员', 1, 'ORGANIZATION', 1620674220008, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_admin', '项目管理员(系统)', '项目管理员', 1, 'PROJECT', 1620674220004, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_member', '项目成员(系统)', '项目成员', 1, 'PROJECT', 1620674220005, 1620674220000, 'admin', 'global');

-- 初始化用户和组的关系
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user) VALUES (uuid(), 'admin', 'admin', 'system', 1684747668375, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user) VALUES (uuid(), 'admin', 'member', 'system', 1684747668375, 'admin');


-- 初始化用户组权限
-- 系统管理员拥有所有的权限，不用初始化

-- 组织管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2ef69c80-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+DELETE_USER', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2ef68cda-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+ADD_USER', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2ef67aba-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+UPDATE_USER', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2eb8cdb1-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_QUOTA:READ+UPDATE', 'ORGANIZATION_QUOTA');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2eb8bc41-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_QUOTA:READ', 'ORGANIZATION_QUOTA');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde1a80-3bc6-11ed-9680-0242ac130008', 'org_admin', 'PERSONAL_INFORMATION:READ+UPDATE_PASSWORD', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde1a2e-3bc6-11ed-9680-0242ac130008', 'org_admin', 'PERSONAL_INFORMATION:READ+API_KEYS', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde19c9-3bc6-11ed-9680-0242ac130008', 'org_admin', 'PERSONAL_INFORMATION:READ+THIRD_ACCOUNT', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2dde18f5-3bc6-11ed-9680-0242ac130008', 'org_admin', 'PERSONAL_INFORMATION:READ+UPDATE', 'PERSONAL_INFORMATION');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d140aab-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+DELETE_GROUP', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d13f889-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+COPY_GROUP', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d13e8c8-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+UPDATE_GROUP', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2d13d1c8-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+CREATE_GROUP', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2cea1857-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+UPLOAD_JAR', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf4318-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_MESSAGE:READ+UPDATE', 'ORGANIZATION_MESSAGE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf15cd-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_MESSAGE:READ', 'ORGANIZATION_MESSAGE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf0246-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_SERVICE:READ+UPDATE', 'ORGANIZATION_SERVICE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caed957-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_SERVICE:READ', 'ORGANIZATION_SERVICE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b817d1d-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_OPERATING_LOG:READ', 'ORGANIZATION_OPERATING_LOG');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b816897-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+DELETE', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81585a-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+UPDATE', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b8146ea-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+EXPORT', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b812d70-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+IMPORT', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b811ba3-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+ADD', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b810b30-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ+COPY', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80fb44-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80e844-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+UPDATE', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80d69a-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+DELETE', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80c5fe-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ+ADD', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80b588-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_PROJECT_MANAGER:READ', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80a580-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_TEMPLATE:READ+CASE_TEMPLATE', 'ORGANIZATION_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b8091ab-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_TEMPLATE:READ+ISSUE_TEMPLATE', 'ORGANIZATION_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b8081e3-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_TEMPLATE:READ', 'ORGANIZATION_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b806fbf-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_USER:READ+UPDATE', 'ORGANIZATION_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b80601c-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_USER:READ+DELETE', 'ORGANIZATION_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b805052-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_USER:READ', 'ORGANIZATION_USER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b802f2a-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_TEMPLATE:READ+CUSTOM', 'ORGANIZATION_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b801d1f-3bc6-11ed-9680-0242ac130008', 'org_admin', 'ORGANIZATION_USER:READ+ADD', 'ORGANIZATION_USER');
-- 组织成员权限
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2cea2a4c-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_PROJECT_MANAGER:READ+UPLOAD_JAR', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caf28b0-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_MESSAGE:READ', 'ORGANIZATION_MESSAGE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2caeef78-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_SERVICE:READ', 'ORGANIZATION_SERVICE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81d869-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_OPERATING_LOG:READ', 'ORGANIZATION_OPERATING_LOG');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81c48b-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_PROJECT_MANAGER:READ', 'ORGANIZATION_PROJECT_MANAGER');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81b3ae-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_PROJECT_ENVIRONMENT:READ', 'ORGANIZATION_PROJECT_ENVIRONMENT');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b81a16c-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_TEMPLATE:READ', 'ORGANIZATION_TEMPLATE');
INSERT INTO user_role_permission (id, role_id, permission_id, module_id) VALUES ('2b818ee7-3bc6-11ed-9680-0242ac130008', 'org_member', 'ORGANIZATION_USER:READ', 'ORGANIZATION_USER');

-- 项目管理员权限

-- 项目成员权限

-- 只读用户的权限

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
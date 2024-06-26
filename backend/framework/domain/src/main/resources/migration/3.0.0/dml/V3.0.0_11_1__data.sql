-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化组织
INSERT INTO organization (id, num, name, description, create_user, update_user, create_time, update_time) VALUES ('100001', 100001, '默认组织', '系统默认创建的组织', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
-- 初始化项目
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting) VALUES ('100001100001', 100001, (SELECT id FROM organization WHERE name LIKE '默认组织'), '示例项目', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["bugManagement","caseManagement","apiTest","testPlan"]');

-- 初始化用户
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user,deleted)
VALUES ('admin', 'Administrator', 'admin@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin',false);

-- 初始化用户组
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('admin', '系统管理员', '拥有系统全部组织以及项目的操作权限', 1, 'SYSTEM', unix_timestamp() * 1000, unix_timestamp() * 1000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('member', '系统成员', '系统内初始化的用户', 1, 'SYSTEM', unix_timestamp() * 1000, unix_timestamp() * 1000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_admin', '组织管理员', '组织管理员', 1, 'ORGANIZATION', unix_timestamp() * 1000, unix_timestamp() * 1000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_member', '组织成员', '组织成员', 1, 'ORGANIZATION', unix_timestamp() * 1000, unix_timestamp() * 1000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_admin', '项目管理员', '项目管理员', 1, 'PROJECT', unix_timestamp() * 1000, unix_timestamp() * 1000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_member', '项目成员', '项目成员', 1, 'PROJECT', unix_timestamp() * 1000, unix_timestamp() * 1000, 'admin', 'global');

-- 初始化用户和组的关系
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES (UUID_SHORT(), 'admin', 'admin', 'system', 'system', unix_timestamp() * 1000, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES (UUID_SHORT(), 'admin', 'org_admin', '100001', '100001', unix_timestamp() * 1000, 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES (UUID_SHORT(), 'admin', 'project_admin', '100001100001', '100001', unix_timestamp() * 1000, 'admin');

-- 初始化用户组权限
-- 系统管理员拥有所有的权限，不用初始化

-- 系统成员的权限
-- 空权限

-- 组织管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_MEMBER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_MEMBER:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_MEMBER:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_MEMBER:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT:READ+RECOVER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT:READ+ADD_MEMBER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT:READ+DELETE_MEMBER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+ENABLE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TASK_CENTER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TASK_CENTER:READ+STOP');

-- 组织成员权限
-- 空权限


-- 项目管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BASE_INFO:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_USER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_USER:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_USER:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_USER:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_GROUP:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_GROUP:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_GROUP:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_GROUP:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_ENVIRONMENT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEMPLATE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEMPLATE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEMPLATE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEMPLATE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_API:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_API:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_CASE:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_BUG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_BUG:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_LOG:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BUG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BUG:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BUG:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BUG:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BUG:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BUG:READ+COMMENT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_BASE_INFO:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEBUG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEBUG:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEBUG:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEBUG:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEBUG:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEBUG:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_CASE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_CASE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_CASE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_CASE:READ+EXECUTE');

INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_MOCK:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_MOCK:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_MOCK:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_MOCK:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_MOCK:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_SCENARIO:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_SCENARIO:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_SCENARIO:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_SCENARIO:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_SCENARIO:READ+EXECUTE');

INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_SCENARIO:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_SCENARIO:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_REPORT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_REPORT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_REPORT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_REPORT:READ+SHARE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_DOC:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_API_DEFINITION_DOC:READ+SHARE');

INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+COMMENT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+MINDER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'CASE_REVIEW:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'CASE_REVIEW:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'CASE_REVIEW:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'CASE_REVIEW:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'CASE_REVIEW:READ+REVIEW');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'CASE_REVIEW:READ+RELEVANCE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_MODULE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_MODULE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_MODULE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+ASSOCIATION');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_CUSTOM_FUNCTION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_CUSTOM_FUNCTION:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_CUSTOM_FUNCTION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_CUSTOM_FUNCTION:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_CUSTOM_FUNCTION:READ+EXECUTE');

-- 项目成员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_BASE_INFO:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_USER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_ENVIRONMENT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_ENVIRONMENT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_ENVIRONMENT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_ENVIRONMENT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_ENVIRONMENT:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_ENVIRONMENT:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_FILE_MANAGEMENT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_FILE_MANAGEMENT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_FILE_MANAGEMENT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_FILE_MANAGEMENT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEMPLATE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_LOG:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_BUG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_BUG:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_BUG:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_BUG:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_BUG:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_BUG:READ+COMMENT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_CUSTOM_FUNCTION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_CUSTOM_FUNCTION:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_CUSTOM_FUNCTION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_CUSTOM_FUNCTION:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_CUSTOM_FUNCTION:READ+EXECUTE');

INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEBUG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEBUG:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEBUG:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEBUG:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEBUG:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEBUG:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_CASE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_CASE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_CASE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_CASE:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_MOCK:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_MOCK:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_MOCK:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_MOCK:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_MOCK:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_SCENARIO:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_SCENARIO:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_SCENARIO:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_SCENARIO:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_SCENARIO:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_SCENARIO:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_SCENARIO:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_REPORT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_REPORT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_REPORT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_REPORT:READ+SHARE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+COMMENT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+IMPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+MINDER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'CASE_REVIEW:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'CASE_REVIEW:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'CASE_REVIEW:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'CASE_REVIEW:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'CASE_REVIEW:READ+REVIEW');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'CASE_REVIEW:READ+RELEVANCE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN_MODULE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN_MODULE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN_MODULE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+ASSOCIATION');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_DOC:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_API_DEFINITION_DOC:READ+SHARE');
-- 初始化当前站点配置
INSERT into system_parameter values('base.url', 'http://127.0.0.1:8081', 'text');

-- 初始化资源池
INSERT INTO test_resource_pool (id, name, type, description, enable, create_time, update_time, create_user, api_test, load_test, ui_test, all_org, deleted) VALUES ('100001100001', '默认资源池', 'Node', '系统初始化资源池', true, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', true, false, false, true, false);
INSERT INTO test_resource_pool_blob (id, configuration) VALUES ('100001100001', 0x504B030414000808080086755258000000000000000000000000030000007A6970758E310F82301085FF4B674CCAA6AC9A28896162230C859E58697BCDB51D0CE1BF5B21B018C7F75DDEFB6E621A85ACC187D288015861A3D6D90E6F20DCC62C4AF077E5032B9A89A9C459107E3C50B4168865CC21A51B3B72CE53326855404AE0942FA047DB4722B0A18AA64B8D22E7739B2D4BAB21E00876D70903DE897EFFE9B7BF7287B27E1208E937F2C2EE020F95FC0AF73D094EE3BB4AAB1B89EA4A4A6E695024CF7F144843F99D6FDAF903504B0708CA0091AFAE00000031010000504B0102140014000808080086755258CA0091AFAE000000310100000300000000000000000000000000000000007A6970504B0506000000000100010031000000DF0000000000);

-- 初始化默认项目与默认资源池的关系
INSERT INTO project_test_resource_pool (project_id, test_resource_pool_id) VALUES ('100001100001', (SELECT id FROM test_resource_pool WHERE name = '默认资源池'));
-- 初始化组织功能用例字段
INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id) VALUES(UUID_SHORT(), 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001');
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority'), 'P0', 'P0', 1, 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority'), 'P1', 'P1', 1, 2);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority'), 'P2', 'P2', 1, 3);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority'), 'P3', 'P3', 1, 4);

-- 初始化组织缺陷严重程度
INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id) VALUES(UUID_SHORT(), 'bug_degree', 'BUG', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001');
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree'), 'suggestive', '提示', 1, 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree'), 'general', '一般', 1, 2);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree'), 'severity', '严重', 1, 3);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree'), 'deadly', '致命', 1, 4);

-- 初始化组织功能用例默认模板, 缺陷默认模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene) VALUES (UUID_SHORT(), 'functional_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 'FUNCTIONAL');
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,scene) VALUES (UUID_SHORT(), 'bug_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 'BUG');
-- 初始化组织默认模板内置字段, 项目默认模板内置字段
INSERT INTO template_custom_field(id, field_id, template_id, required, pos, system_field, api_field_id, default_value) VALUES(UUID_SHORT(), (select id from custom_field where name = 'functional_priority'), (select id from template where name = 'functional_default'), 1, 0, 0, NULL, NULL);
INSERT INTO template_custom_field(id, field_id, template_id, required, pos, system_field, api_field_id, default_value) VALUES(UUID_SHORT(), (select id from custom_field where name = 'bug_degree'), (select id from template where name = 'bug_default'), 1, 0, 0, NULL, NULL);


-- 初始化默认项目版本
INSERT INTO project_version (id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user) VALUES ('100000000000001', '100001100001', 'v1.0', NULL, 'open', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin');

-- 初始化项目功能用例字段
INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id, ref_id) VALUES(UUID_SHORT(), 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001100001', (SELECT id FROM (SELECT * FROM custom_field) t where name = 'functional_priority'));
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P0', 'P0', 1, 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P1', 'P1', 1, 2);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P2', 'P2', 1, 3);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P3', 'P3', 1, 4);

-- 初始化项目缺陷严重程度
INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id, ref_id) VALUES(UUID_SHORT(), 'bug_degree', 'BUG', 'SELECT', '', 1, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001100001', (SELECT id FROM (SELECT * FROM custom_field) t where name = 'bug_degree'));
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree' and scope_id = '100001100001'), 'suggestive', '提示', 1, 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree' and scope_id = '100001100001'), 'general', '一般', 1, 2);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree' and scope_id = '100001100001'), 'severity', '严重', 1, 3);
INSERT INTO custom_field_option (field_id,value,`text`,internal, pos) VALUES ((select id from custom_field where name = 'bug_degree' and scope_id = '100001100001'), 'deadly', '致命', 1, 4);

-- 初始化项目功能用例默认模板, 缺陷默认模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id) VALUES (UUID_SHORT(), 'functional_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 'FUNCTIONAL', (SELECT id FROM (SELECT * FROM template) t where name = 'functional_default'));
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id) VALUES (UUID_SHORT(), 'bug_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 'BUG', (SELECT id FROM (SELECT * FROM template) t where name = 'bug_default'));
-- 初始化项目默认模板内置字段, 项目默认模板内置字段
INSERT INTO template_custom_field(id, field_id, template_id, required, pos, system_field, api_field_id, default_value) VALUES(UUID_SHORT(), (select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), (select id from template where name = 'functional_default' and scope_id = '100001100001'), 1, 0, 0, NULL, null);
INSERT INTO template_custom_field(id, field_id, template_id, required, pos, system_field, api_field_id, default_value) VALUES(UUID_SHORT(), (select id from custom_field where name = 'bug_degree' and scope_id = '100001100001'), (select id from template where name = 'bug_default' and scope_id = '100001100001'), 1, 0, 0, NULL, null);


-- 初始化组织接口模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,scene) VALUES (UUID_SHORT(), 'api_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 'API');

-- 初始化项目接口模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id) VALUES (UUID_SHORT(), 'api_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 'API', (SELECT id FROM (SELECT * FROM template) t where name = 'api_default'));

-- 初始化组织UI模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,scene) VALUES (UUID_SHORT(), 'ui_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 'UI');

-- 初始化项目UI模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id) VALUES (UUID_SHORT(), 'ui_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 'UI', (SELECT id FROM (SELECT * FROM template) t where name = 'ui_default'));

-- 初始化组织测试计划模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,scene) VALUES (UUID_SHORT(), 'test_plan_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 'TEST_PLAN');

-- 初始化项目测试计划模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, scene, ref_id) VALUES (UUID_SHORT(), 'test_plan_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 'TEST_PLAN', (SELECT id FROM (SELECT * FROM template) t where name = 'test_plan_default'));

-- 初始化组织缺陷状态项
-- 新建
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_new', 'BUG', NULL, 1, 'ORGANIZATION', NULL, '100001', 0);
-- 处理中
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_in_process', 'BUG', NULL, 1, 'ORGANIZATION', NULL, '100001', 1);
-- 已关闭
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_closed', 'BUG', NULL, 1, 'ORGANIZATION', NULL, '100001', 2);
-- 已解决
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_resolved', 'BUG', NULL, 1, 'ORGANIZATION', NULL, '100001', 3);
-- 已拒绝
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_rejected', 'BUG', NULL, 1, 'ORGANIZATION', NULL, '100001', 4);

-- 初始化组织缺陷状态定义
INSERT INTO status_definition(status_id, definition_id) VALUES((SELECT id FROM status_item where name = 'bug_new'), 'START');
INSERT INTO status_definition(status_id, definition_id) VALUES((SELECT id FROM status_item where name = 'bug_closed'), 'END');
INSERT INTO status_definition(status_id, definition_id) VALUES((SELECT id FROM status_item where name = 'bug_rejected'), 'END');

-- 初始化组织缺陷状态流
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_new'), (SELECT id FROM status_item where name = 'bug_in_process'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_new'), (SELECT id FROM status_item where name = 'bug_rejected'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_in_process'), (SELECT id FROM status_item where name = 'bug_rejected'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_in_process'), (SELECT id FROM status_item where name = 'bug_resolved'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_in_process'), (SELECT id FROM status_item where name = 'bug_closed'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_closed'), (SELECT id FROM status_item where name = 'bug_in_process'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_resolved'), (SELECT id FROM status_item where name = 'bug_in_process'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_resolved'), (SELECT id FROM status_item where name = 'bug_closed'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_rejected'), (SELECT id FROM status_item where name = 'bug_in_process'));

-- 初始化项目缺陷状态项
-- 新建
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_new', 'BUG', NULL, 1, 'PROJECT', (SELECT id FROM (SELECT * FROM status_item) t where name = 'bug_new'), '100001100001', 0);
-- 处理中
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_in_process', 'BUG', NULL, 1, 'PROJECT', (SELECT id FROM (SELECT * FROM status_item) t where name = 'bug_in_process'), '100001100001', 1);
-- 已解决
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_resolved', 'BUG', NULL, 1, 'PROJECT', (SELECT id FROM (SELECT * FROM status_item) t where name = 'bug_resolved'), '100001100001', 2);
-- 已关闭
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_closed', 'BUG', NULL, 1, 'PROJECT', (SELECT id FROM (SELECT * FROM status_item) t where name = 'bug_closed'), '100001100001', 3);
-- 已拒绝
INSERT INTO status_item(id, name, scene, remark, internal, scope_type, ref_id, scope_id, pos) VALUES(UUID_SHORT(), 'bug_rejected', 'BUG', NULL, 1, 'PROJECT', (SELECT id FROM (SELECT * FROM status_item) t where name = 'bug_rejected'), '100001100001', 4);

-- 初始化项目缺陷状态定义
INSERT INTO status_definition(status_id, definition_id) VALUES((SELECT id FROM status_item where name = 'bug_new' and scope_id = '100001100001'), 'START');
INSERT INTO status_definition(status_id, definition_id) VALUES((SELECT id FROM status_item where name = 'bug_closed' and scope_id = '100001100001'), 'END');
INSERT INTO status_definition(status_id, definition_id) VALUES((SELECT id FROM status_item where name = 'bug_rejected' and scope_id = '100001100001'), 'END');

-- 初始化项目缺陷状态流
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_new' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_in_process' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_new' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_rejected' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_in_process' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_rejected' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_in_process' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_resolved' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_in_process' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_closed' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_closed' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_in_process' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_resolved' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_in_process' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_resolved' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_closed' and scope_id = '100001100001'));
INSERT INTO status_flow(id, from_id, to_id) VALUES(UUID_SHORT(), (SELECT id FROM status_item where name = 'bug_rejected' and scope_id = '100001100001'), (SELECT id FROM status_item where name = 'bug_in_process' and scope_id = '100001100001'));

-- 初始化内置消息机器人
SET @robot_in_site_id = UUID_SHORT();
Insert into project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES (@robot_in_site_id, '100001100001', 'robot_in_site', 'IN_SITE', 'NONE', null, null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, 'robot_in_site_description');
Insert into project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES (UUID_SHORT(), '100001100001', 'robot_mail', 'MAIL', 'NONE', null, null, null, false, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, 'robot_mail_description');

-- 初始化消息设置数据
-- 初始化测试计划相关的消息数据
SET @test_plan_task_update_creator_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@test_plan_task_update_creator_id, 'UPDATE', '["CREATE_USER", "FOLLOW_PEOPLE"]', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_update_creator_id, 'message.test_plan_task_update');

SET @test_plan_task_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@test_plan_task_delete_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_delete_id, 'message.test_plan_task_update');

SET @test_plan_task_execute_success_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@test_plan_task_execute_success_id, 'EXECUTE_SUCCESSFUL', '["CREATE_USER"]', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_execute_success');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_execute_success_id, 'message.test_plan_task_execute');

SET @test_plan_task_execute_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@test_plan_task_execute_id, 'EXECUTE_FAILED', '["OPERATOR"]', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_execute_failed');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_execute_id, 'message.test_plan_task_execute');

SET @test_plan_task_report_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@test_plan_task_report_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'TEST_PLAN_REPORT_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_report_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_report_id, 'message.test_plan_report_task_delete');

-- 初始化缺陷相关的消息数据
SET @bug_update_creator_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@bug_update_creator_id, 'UPDATE', '["CREATE_USER", "FOLLOW_PEOPLE"]', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@bug_update_creator_id, 'message.bug_task_update');

SET @bug_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@bug_delete_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@bug_delete_id, 'message.bug_task_delete');

SET @bug_comment_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@bug_comment_id, 'COMMENT', '["CREATE_USER"]', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@bug_comment_id, 'message.bug_task_comment');

SET @bug_sync_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@bug_sync_id, 'EXECUTE_COMPLETED', '["OPERATOR"]', @robot_in_site_id, 'BUG_SYNC_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_sync_task_execute_completed');
INSERT INTO message_task_blob(id, template) VALUES (@bug_sync_id, 'message.bug_sync_task_execute_completed');

-- 初始化用例管理消息设置数据
SET @functional_creator_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@functional_creator_id, 'UPDATE', '["CREATE_USER", "FOLLOW_PEOPLE"]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@functional_creator_id, 'message.functional_case_task_update');

SET @functional_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@functional_delete_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@functional_delete_id, 'message.functional_case_task_delete');

SET @functional_review_passed_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@functional_review_passed_id, 'REVIEW_PASSED', '["CREATE_USER"]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_passed');
INSERT INTO message_task_blob(id, template) VALUES (@functional_review_passed_id, 'message.functional_case_task_review');

SET @functional_review_fail_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@functional_review_fail_id, 'REVIEW_FAIL', '["CREATE_USER"]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_fail');
INSERT INTO message_task_blob(id, template) VALUES (@functional_review_fail_id, 'message.functional_case_task_review');

SET @functional_execute_passed_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@functional_execute_passed_id, 'EXECUTE_PASSED', '["CREATE_USER"]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_execute_passed');
INSERT INTO message_task_blob(id, template) VALUES (@functional_execute_passed_id, 'message.functional_case_task_plan');

SET @functional_execute_fail_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@functional_execute_fail_id, 'EXECUTE_FAIL', '["CREATE_USER"]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_execute_fail');
INSERT INTO message_task_blob(id, template) VALUES (@functional_execute_fail_id, 'message.functional_case_task_plan');

SET @functional_comment_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@functional_comment_id, 'COMMENT', '["CREATE_USER"]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@functional_comment_id, 'message.functional_case_task_comment');


SET @case_creator_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@case_creator_id, 'UPDATE', '["CREATE_USER", "FOLLOW_PEOPLE"]', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@case_creator_id, 'message.case_review_task_update');

SET @case_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@case_delete_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@case_delete_id, 'message.case_review_task_delete');

SET @case_review_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@case_review_id, 'REVIEW_COMPLETED', '["CREATE_USER"]', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_review_completed');
INSERT INTO message_task_blob(id, template) VALUES (@case_review_id, 'message.case_review_task_review_completed');

-- 初始化接口文档消息数据
SET @api_creator_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_creator_id, 'UPDATE', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@api_creator_id, 'message.api_definition_task_update');

SET @api_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_delete_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_delete_id, 'message.api_definition_task_delete');

SET @api_case_update_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_case_update_id, 'CASE_UPDATE', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_update');
INSERT INTO message_task_blob(id, template) VALUES (@api_case_update_id, 'message.api_definition_task_case_update');

SET @api_case_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_case_delete_id, 'CASE_DELETE', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_case_delete_id, 'message.api_definition_task_case_delete');

SET @api_execute_success_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_execute_success_id, 'CASE_EXECUTE_SUCCESSFUL', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_execute_successful');
INSERT INTO message_task_blob(id, template) VALUES (@api_execute_success_id, 'message.api_definition_task_case_execute');

SET @api_fake_error_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_fake_error_id, 'CASE_EXECUTE_FAKE_ERROR', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_execute_fake_error');
INSERT INTO message_task_blob(id, template) VALUES (@api_fake_error_id, 'message.api_definition_task_case_execute');

SET @api_execute_failed_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_execute_failed_id, 'CASE_EXECUTE_FAILED', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_execute_failed');
INSERT INTO message_task_blob(id, template) VALUES (@api_execute_failed_id, 'message.api_definition_task_case_execute');

SET @api_mock_update_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_mock_update_id, 'MOCK_UPDATE', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_mock_update');
INSERT INTO message_task_blob(id, template) VALUES (@api_mock_update_id, 'message.api_definition_task_mock_update');

SET @api_mock_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_mock_delete_id, 'MOCK_DELETE', '["CREATE_USER"]', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_mock_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_mock_delete_id, 'message.api_definition_task_mock_delete');

SET @api_fake_error_id = UUID_SHORT();
INSERT INTO message_task_blob(id, template) VALUES (@api_fake_error_id, 'message.api_definition_task_case_execute');


-- 初始化接口场景消息设置数据
SET @api_scenario_update_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_scenario_update_id, 'UPDATE', '["CREATE_USER"]', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_update_id, 'message.api_scenario_task_update');

SET @api_scenario_delete_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_scenario_delete_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_delete_id, 'message.api_scenario_task_delete');

SET @api_scenario_execute_success_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_scenario_execute_success_id, 'SCENARIO_EXECUTE_SUCCESSFUL', '["CREATE_USER"]', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_scenario_execute_successful');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_execute_success_id, 'message.api_scenario_task_scenario_execute');

SET @api_scenario_fake_error_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_scenario_fake_error_id, 'SCENARIO_EXECUTE_FAKE_ERROR', '["CREATE_USER"]', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_scenario_execute_fake_error');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_fake_error_id, 'message.api_scenario_task_scenario_execute');

SET @api_scenario_execute_failed_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_scenario_execute_failed_id, 'SCENARIO_EXECUTE_FAILED', '["CREATE_USER"]', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_scenario_execute_failed');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_execute_failed_id, 'message.api_scenario_task_scenario_execute');

SET @api_scenario_report_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@api_scenario_report_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'API_REPORT_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_report_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_report_id, 'message.api_report_task_delete');

-- 初始化Jenkins消息数据
SET @jenkins_execute_successful_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@jenkins_execute_successful_id, 'EXECUTE_SUCCESSFUL', '["CREATE_USER"]', @robot_in_site_id, 'JENKINS_TASK', 'NONE', '100001100001', false, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.jenkins_task_execute_successful');
INSERT INTO message_task_blob(id, template) VALUES (@jenkins_execute_successful_id, 'message.jenkins_task_execute');

SET @jenkins_execute_failed_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@jenkins_execute_failed_id, 'EXECUTE_FAILED', '["CREATE_USER"]', @robot_in_site_id, 'JENKINS_TASK', 'NONE', '100001100001', false, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.jenkins_task_execute_failed');
INSERT INTO message_task_blob(id, template) VALUES (@jenkins_execute_failed_id, 'message.jenkins_task_execute');


-- 初始化定时任务消息数据
SET @schedule_open_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@schedule_open_id, 'OPEN', '["CREATE_USER"]', @robot_in_site_id, 'SCHEDULE_TASK', 'NONE', '100001100001', false, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.schedule_task_open');
INSERT INTO message_task_blob(id, template) VALUES (@schedule_open_id, 'message.schedule_task_open');

SET @schedule_close_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@schedule_close_id, 'CLOSE', '["CREATE_USER"]', @robot_in_site_id, 'SCHEDULE_TASK', 'NONE', '100001100001', false, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.schedule_task_close');
INSERT INTO message_task_blob(id, template) VALUES (@schedule_close_id, 'message.schedule_task_close');

-- 初始化内部at 消息通知
SET @case_comment_at_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@case_comment_at_id, 'AT', '[]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@case_comment_at_id, 'message.functional_case_task_at_comment');

SET @case_comment_reply_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@case_comment_reply_id, 'REPLY', '[]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@case_comment_reply_id, 'message.functional_case_task_reply_comment');

SET @case_review_at_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@case_review_at_id, 'REVIEW_AT', '[]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_review_at');
INSERT INTO message_task_blob(id, template) VALUES (@case_review_at_id, 'message.functional_case_task_review_at');

SET @case_plan_at_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@case_plan_at_id, 'EXECUTE_AT', '[]', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_execute_at');
INSERT INTO message_task_blob(id, template) VALUES (@case_plan_at_id, 'message.functional_case_task_plan_at');

SET @bug_comment_at_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@bug_comment_at_id, 'AT', '[]', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@bug_comment_at_id, 'message.bug_task_at_comment');

SET @bug_comment_reply_id = UUID_SHORT();
Insert into message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject) VALUES (@bug_comment_reply_id, 'REPLY', '[]', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@bug_comment_reply_id, 'message.bug_task_reply_comment');




-- 初始化默认项目版本配置项
INSERT INTO project_application (`project_id`, `type`, `type_value`) VALUES ('100001100001', 'VERSION_ENABLE', 'FALSE');

-- 初始化默认项目mock环境
INSERT INTO environment (`id`, `project_id`, `name`, `create_user`, `create_time`, `update_user`, `update_time`, `mock`,`pos`) VALUES (UUID_SHORT(), '100001100001', 'Mock环境', 'admin', unix_timestamp() * 1000, 'admin', unix_timestamp() * 1000, true, 64);
INSERT INTO environment_blob (id,config) VALUES ((SELECT id FROM environment where name = 'Mock环境'), 0x504B030414000808080089706858000000000000000000000000030000007A6970CD52CB4EC33010FC179FD3A62D02A1DC1042E2D287A0E282382C8ED358385EE3475155E5DF5937711BA0121CE9293B3B3B3BB3EE9E716C1AD42BB0D03856EC9915EF4138BF968DC0E059713589BF8C7067503BF1ADD166BDC2135809AF4A90C8F34BC66AEFCD2DEA4A6EA8DE3359B24207A532662C7AE4A858C1EED7EB15232A3AAFA11184C42957E479E346DB8B7125FD8C2B0CE59856E40DF2B79113762B6C3E9D4CA6D79734EB7726CE2D968B3BAA0CF87A0E9ED70F41891886A32EA597A88FDB89D17D93F1064BE27D19E8A0430622D4024A61FB44A570DC4A3354434B6D564C7E482DBBC6AC8D73E0E11183E5C7D350DE749A3D133A5E2D29C65EDA0EC1D7275EA0E8DD955212E73EC840AAE92CB2921C86FE9C53C345DEEE52A79287945D518A0A82F2374A428FB56D7C28B1B248AE1DDA930A187906350AF43938412992E314D74AFC0BB7FF23FE4E3D98C5B3CC7FEA969E4ED8F85003A7094A8246858DEC097330C469DB4F504B0708C82FCAE869010000AE030000504B0102140014000808080089706858C82FCAE869010000AE0300000300000000000000000000000000000000007A6970504B05060000000001000100310000009A0100000000);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

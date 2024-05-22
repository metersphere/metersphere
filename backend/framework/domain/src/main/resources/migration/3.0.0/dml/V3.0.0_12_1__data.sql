-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 处理用例历史数据
update functional_case set last_execute_result = 'PENDING' where last_execute_result = 'UN_EXECUTED';
update functional_case set last_execute_result = 'SUCCESS' where last_execute_result = 'PASSED';
update functional_case set last_execute_result = 'ERROR' where last_execute_result = 'FAILED';
update functional_case set last_execute_result = 'PENDING' where last_execute_result = 'SKIPPED';

-- 初始化计划相关的权限 (删除V3.0.0_11_1测试计划模块相关的权限, 重新初始化, 模块树不单独拥有权限)
delete from user_role_permission where permission_id like 'PROJECT_TEST_PLAN_MODULE%' or permission_id like 'PROJECT_TEST_PLAN%';
-- 项目管理员(测试计划相关的权限)
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN:READ+ASSOCIATION');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_REPORT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_REPORT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_REPORT:READ+SHARE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEST_PLAN_REPORT:READ+DELETE');
-- 项目成员(测试计划相关的权限)
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+EXECUTE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN:READ+ASSOCIATION');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN_REPORT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN_REPORT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN_REPORT:READ+SHARE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_TEST_PLAN_REPORT:READ+DELETE');

INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_TEST_PLAN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_TEST_PLAN:UPDATE');

-- 已存在的项目都开启测试计划模块
UPDATE project set module_setting = '["bugManagement","caseManagement","apiTest","testPlan"]';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

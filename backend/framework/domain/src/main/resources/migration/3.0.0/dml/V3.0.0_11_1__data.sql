-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化组织
INSERT INTO organization (id, num, name, description, create_user, update_user, create_time, update_time) VALUES ('100001', 100001, '默认组织', '系统默认创建的组织', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
-- 初始化项目
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time, module_setting) VALUES ('100001100001', 100001, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000,'["workstation","loadTest","testPlan","bugManagement","caseManagement","apiTest","uiTest"]');

-- 初始化用户
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user,deleted)
VALUES ('admin', 'Administrator', 'admin@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin',false);

-- 初始化用户组
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('admin', '系统管理员', '拥有系统全部组织以及项目的操作权限', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('member', '系统成员', '系统内初始化的用户', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_admin', '组织管理员', '组织管理员', 1, 'ORGANIZATION', 1620674220007, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('org_member', '组织成员', '组织成员', 1, 'ORGANIZATION', 1620674220008, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_admin', '项目管理员', '项目管理员', 1, 'PROJECT', 1620674220004, 1620674220000, 'admin', 'global');
INSERT INTO user_role (id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUES ('project_member', '项目成员', '项目成员', 1, 'PROJECT', 1620674220005, 1620674220000, 'admin', 'global');

-- 初始化用户和组的关系
INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES (UUID_SHORT(), 'admin', 'admin', 'system', 'system', 1684747668375, 'admin');

-- 初始化用户组权限
-- 系统管理员拥有所有的权限，不用初始化

-- 系统成员的权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_USER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_ORGANIZATION_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_USER_ROLE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_TEST_RESOURCE_POOL:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_PLUGIN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_PARAMETER_SETTING_BASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_PARAMETER_SETTING_DISPLAY:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_PARAMETER_SETTING_AUTH:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_AUTH:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'member', 'SYSTEM_LOG:READ');

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
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT_MEMBER:ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_PROJECT_MEMBER:DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_CUSTOM_FIELD:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_CUSTOM_FIELD:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_CUSTOM_FIELD:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_CUSTOM_FIELD:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_TEMPLATE:READ+DELETE');

-- 组织成员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_member', 'ORGANIZATION_USER_ROLE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_member', 'ORGANIZATION_MEMBER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_member', 'SYSTEM_SERVICE_INTEGRATION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_member', 'ORGANIZATION_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_member', 'ORGANIZATION_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_member', 'ORGANIZATION_CUSTOM_FIELD:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_member', 'ORGANIZATION_TEMPLATE:READ');

-- 项目管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT:READ');
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
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FILE_MANAGEMENT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_TEMPLATE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_MESSAGE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_VERSION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_VERSION:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_VERSION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_VERSION:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_FAKE_ERROR:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_TEST_PLAN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_TEST_PLAN:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_UI:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_UI:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_PERFORMANCE_TEST:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_PERFORMANCE_TEST:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_API:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_API:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_CASE:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_ISSUE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_ISSUE:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_WORKSTATION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_APPLICATION_WORKSTATION:UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_LOG:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_LOG:UPDATE');

-- 项目成员权限

-- 初始化当前站点配置
INSERT into system_parameter values('base.url', 'http://127.0.0.1:8081', 'text');
-- 初始化prometheus站点配置
INSERT into system_parameter values('base.prometheus.host', 'http://ms-prometheus:9090', 'text');

-- 初始化资源池
INSERT INTO test_resource_pool (id, name, type, description, enable, create_time, update_time, create_user, api_test, load_test, ui_test, all_org, deleted) VALUES (uuid_short(), 'LOCAL', 'Node', '系统初始化资源池', true, 1690440108595, 1690440110182, 'admin', true, true, true, true, false);
INSERT INTO test_resource_pool_blob (id, configuration) VALUES ((select id from test_resource_pool where name = 'LOCAL'), 0x504B03041400080808004F76FC56000000000000000000000000030000007A6970ED58C18EA33810FD1584E618266969B4D272A3136F9A9E8444408F76351921079CC413B09131D989A2FCFB96A1932604D3873DADB63941D5F3F3ABA2EC329CCC94E32424857433BC25A66D9A83ABE989E0BCB6309E9062460B69DADF4F265556898BBD254AC6880040CE05F8CCDF46A3113C659C51C905187E7FA80C316771290461D22BB3358CB0C17EFE31A8A85899A60353F23D619707863312E43826C5C572CF50DB739E843B41707245FEE4EB09D950504039109A38A7DF8828E0C136D658C6BBE1E161C5F69425B6F1CCD72B961189132CB1BD628691E235498BEAD6302424C1A280FB740A511046EEE4AC1C4A9DB23D2F1E23CF992330163989AB413916384D494A8BCC361E9445922C4FB124AF9C37B3A9EB66C69E590DE33A89BAF0A68AF1D81808B97098A4CEBD077C826C08E42F999482B26D10EF4852A670E76E19BF9AD12F12972A6F37630DC3AAB85F794322B296FF1A47405212C3ABEFF043E82AFBE8174829D4EB283A416AB23D39DA973474630C83E744603593E1321DE680D392E8A6A9A76AE7B879499EF3946F8F5F959C7D0955C708A8FA4CF970C70BA9CAA03DE86F42B73B096F7E34BAB8A07225A6B04C9A422C306719861A6C27BAD8B52D56DCB60C61DD592A3F9F6FD1841DEEF8EA620D42C70FA3D09DA3B6E22A47B6B1323F9DDE40E795D9CD33F51DEF65E6F86EF8570F5103A5657A9EA310F9918F960B3F0CA270B174C71A4A58691DE073376F850C964FC847D18B3FD352B6701A361F058B177F8CA040B44C0D8C86E5D1197F45DE249AB941883CE4F7A4AE0DD5E6EF71B10883D077965180FC6F108A56DF1D5217AB13BA8B1E69955FABA77E37D11FAEE7E8925E9134605AAED725A98DA87BC95E473FF9C89944DECBBC47C71B48ABE209394BAD04E5D4D64C15626FC5BC22FA1940E1EC9D25DB42F6BC9D4B8D7A13F4672F6113A8E59B2DA6EF2CD92B4213E3741C39B3A9AEDE600FA9FD37A3A93AA334F60377EE4CD13D6209E780254F690CDBB6BBF1B85C42C781934313588BF809DD98889B3E09079976C7B0DEF6F025B86D431D73466DE5B9E092C73C85021CDFD4CD81A76546E6BC641DCC99322FB1DCD9C650EDE86DD65A67D50B37342545DFF83A1A0B5A56370B38DA24FFB20F6D09535D985882A8C47DB4A33BDE8F76D4D6F7D18E3A747CB4231DE1FFA91DD5BBE87FA377400C120B7909CC2387B7465AABBEFDDE81AF60799C50F85E3B9DEF23EF12F9DE98862473A03EF31BBF31EA5F0109C9537EF4007DB194742A6862DAE64ECADC1E0E0BF85E65B4CCAC5DB9B6BFC0054C5B2A92F1DDEF862F03938BADABFE337CFF71FE07504B07081EC0585CBB03000038110000504B010214001400080808004F76FC561EC0585CBB030000381100000300000000000000000000000000000000007A6970504B0506000000000100010031000000EC0300000000);

-- 初始化组织功能用例字段
INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES(UUID_SHORT(), 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001');
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority'), 'P0', 'P0', 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority'), 'P1', 'P1', 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority'), 'P2', 'P2', 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority'), 'P3', 'P3', 1);

-- 初始化组织功能用例模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,enable_default, scene)
VALUES (UUID_SHORT(), 'functional_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 1, 'FUNCTIONAL');
INSERT INTO template_custom_field(id, field_id, template_id, required, pos, api_field_id, default_value)
VALUES(UUID_SHORT(), (select id from custom_field where name = 'functional_priority'), (select id from template where name = 'functional_default'), 1, 0, NULL, NULL);

-- 初始化默认项目版本
INSERT INTO project_version (id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user) VALUES (UUID_SHORT(), '100001100001', 'v1.0', NULL, 'open', 0, NULL, NULL, NULL, UNIX_TIMESTAMP() * 1000, 'admin');

-- 初始化项目功能用例字段
INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id, ref_id)
VALUES(UUID_SHORT(), 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', '100001100001',
       (SELECT id FROM (SELECT * FROM custom_field) t where name = 'functional_priority'));
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P0', 'P0', 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P1', 'P1', 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P2', 'P2', 1);
INSERT INTO custom_field_option (field_id,value,`text`,internal)
VALUES ((select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'), 'P3', 'P3', 1);

-- 初始化项目功能用例模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, enable_default, scene, ref_id)
VALUES (UUID_SHORT(), 'functional_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 1, 'FUNCTIONAL',
        (SELECT id FROM (SELECT * FROM template) t where name = 'functional_default'));
INSERT INTO template_custom_field(id, field_id, template_id, required, pos, api_field_id, default_value)
VALUES(
  UUID_SHORT(),
  (select id from custom_field where name = 'functional_priority' and scope_id = '100001100001'),
  (select id from template where name = 'functional_default' and scope_id = '100001100001'),
  1, 0, NULL, null
);

-- 初始化组织缺陷模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,enable_default,scene)
VALUES (UUID_SHORT(), 'bug_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 1, 'BUG');

-- 初始化项目缺陷模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, enable_default, scene, ref_id)
VALUES (UUID_SHORT(), 'bug_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 1, 'BUG',
        (SELECT id FROM (SELECT * FROM template) t where name = 'bug_default'));

-- 初始化组织接口模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,enable_default,scene)
VALUES (UUID_SHORT(), 'api_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 1, 'API');

-- 初始化项目接口模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, enable_default, scene, ref_id)
VALUES (UUID_SHORT(), 'api_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 1, 'API',
        (SELECT id FROM (SELECT * FROM template) t where name = 'api_default'));

-- 初始化组织UI模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,enable_default,scene)
VALUES (UUID_SHORT(), 'ui_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 1, 'UI');

-- 初始化项目UI模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, enable_default, scene, ref_id)
VALUES (UUID_SHORT(), 'ui_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 1, 'UI',
        (SELECT id FROM (SELECT * FROM template) t where name = 'ui_default'));

-- 初始化组织测试计划模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part,enable_default,scene)
VALUES (UUID_SHORT(), 'test_plan_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'ORGANIZATION', '100001', 0, 1, 'TEST_PLAN');

-- 初始化项目测试计划模板
INSERT INTO template (id,name,remark,internal,update_time,create_time,create_user,scope_type,scope_id,enable_third_part, enable_default, scene, ref_id)
VALUES (UUID_SHORT(), 'test_plan_default', '', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'PROJECT', '100001100001', 0, 1, 'TEST_PLAN',
        (SELECT id FROM (SELECT * FROM template) t where name = 'test_plan_default'));

-- 初始化内置消息机器人
SET @robot_in_site_id = UUID_SHORT();
Insert into project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description)
VALUES (@robot_in_site_id, '100001100001', '站内信', 'IN_SITE', 'NONE', null, null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);
Insert into project_robot(id, project_id, name, platform, webhook, type, app_key, app_secret, enable, create_user, create_time, update_user, update_time, description) VALUES (UUID_SHORT(), '100001100001', '邮件', 'MAIL', 'NONE', null, null, null, true, 'admin', unix_timestamp() * 1000,'admin',  unix_timestamp() * 1000, null);

-- 初始化消息设置数据
-- 初始化测试计划相关的消息数据
SET @test_plan_task_update_creator_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@test_plan_task_update_creator_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_update_creator_id, 'message.test_plan_task_update');

SET @test_plan_task_update_follower_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@test_plan_task_update_follower_id, 'UPDATE', 'FOLLOW_PEOPLE', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_update_follower_id, 'message.test_plan_task_update');

SET @test_plan_task_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@test_plan_task_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_delete_id, 'message.test_plan_task_update');

SET @test_plan_task_execute_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@test_plan_task_execute_id, 'EXECUTE_COMPLETED', 'OPERATOR', @robot_in_site_id, 'TEST_PLAN_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_task_execute_completed');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_execute_id, 'message.test_plan_task_execute_completed');

SET @test_plan_task_report_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@test_plan_task_report_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'TEST_PLAN_REPORT_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_report_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@test_plan_task_report_id, 'message.test_plan_report_task_delete');

-- 初始化缺陷相关的消息数据
SET @bug_update_creator_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@bug_update_creator_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@bug_update_creator_id, 'message.bug_task_update');

SET @bug_update_follower_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@bug_update_follower_id, 'UPDATE', 'FOLLOW_PEOPLE', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@bug_update_follower_id, 'message.bug_task_update');

SET @bug_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@bug_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@bug_delete_id, 'message.bug_task_delete');

SET @bug_comment_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@bug_comment_id, 'COMMENT', 'CREATE_USER', @robot_in_site_id, 'BUG_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@bug_comment_id, 'message.bug_task_comment');

SET @bug_comment_at_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@bug_comment_at_id, 'COMMENT', 'CREATE_USER', @robot_in_site_id, 'BUG_TASK_AT', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@bug_comment_at_id, 'message.bug_task_at_comment');

SET @bug_sync_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@bug_sync_id, 'EXECUTE_COMPLETED', 'OPERATOR', @robot_in_site_id, 'BUG_SYNC_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.bug_sync_task_execute_completed');
INSERT INTO message_task_blob(id, template) VALUES (@bug_sync_id, 'message.bug_sync_task_execute_completed');

-- 初始化用例管理消息设置数据
SET @functional_creator_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@functional_creator_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@functional_creator_id, 'message.functional_case_task_update');

SET @functional_follower_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@functional_follower_id, 'UPDATE', 'FOLLOW_PEOPLE', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@functional_follower_id, 'message.functional_case_task_update');

SET @functional_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@functional_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@functional_delete_id, 'message.functional_case_task_delete');

SET @functional_comment_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@functional_comment_id, 'COMMENT', 'CREATE_USER', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@functional_comment_id, 'message.functional_case_task_comment');

SET @functional_comment_at_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@functional_comment_at_id, 'COMMENT', 'CREATE_USER', @robot_in_site_id, 'FUNCTIONAL_CASE_TASK_AT', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.functional_case_task_comment');
INSERT INTO message_task_blob(id, template) VALUES (@functional_comment_at_id, 'message.functional_case_task_at_comment');

SET @case_creator_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@case_creator_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@case_creator_id, 'message.case_review_task_update');

SET @case_follower_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@case_follower_id, 'UPDATE', 'FOLLOW_PEOPLE', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@case_follower_id, 'message.case_review_task_update');

SET @case_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@case_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@case_delete_id, 'message.case_review_task_delete');

SET @case_execute_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@case_execute_id, 'EXECUTE_REVIEW', 'CREATE_USER', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_execute_review');
INSERT INTO message_task_blob(id, template) VALUES (@case_execute_id, 'message.case_review_task_execute_review');

SET @case_review_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@case_review_id, 'REVIEW_COMPLETED', 'CREATE_USER', @robot_in_site_id, 'CASE_REVIEW_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.case_review_task_review_completed');
INSERT INTO message_task_blob(id, template) VALUES (@case_review_id, 'message.case_review_task_review_completed');

-- 初始化接口文档消息数据
SET @api_creator_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_creator_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@api_creator_id, 'message.api_definition_task_update');

SET @api_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_delete_id, 'message.api_definition_task_delete');

SET @api_case_update_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_case_update_id, 'CASE_UPDATE', 'CREATE_USER', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_update');
INSERT INTO message_task_blob(id, template) VALUES (@api_case_update_id, 'message.api_definition_task_case_update');

SET @api_case_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_case_delete_id, 'CASE_DELETE', 'CREATE_USER', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_case_delete_id, 'message.api_definition_task_delete');

SET @api_execute_success_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_execute_success_id, 'CASE_EXECUTE_SUCCESSFUL', 'CREATE_USER', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_execute_successful');
INSERT INTO message_task_blob(id, template) VALUES (@api_execute_success_id, 'message.api_definition_task_case_execute');

SET @api_fake_error_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_fake_error_id, 'CASE_EXECUTE_FAKE_ERROR', 'CREATE_USER', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_execute_fake_error');
INSERT INTO message_task_blob(id, template) VALUES (@api_fake_error_id, 'message.api_definition_task_case_execute');

SET @api_execute_failed_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_execute_failed_id, 'CASE_EXECUTE_FAILED', 'CREATE_USER', @robot_in_site_id, 'API_DEFINITION_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_definition_task_case_execute_failed');
INSERT INTO message_task_blob(id, template) VALUES (@api_execute_failed_id, 'message.api_definition_task_case_execute');

-- 初始化接口场景消息设置数据
SET @api_scenario_update_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_scenario_update_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_update_id, 'message.api_scenario_task_update');

SET @api_scenario_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_scenario_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_delete_id, 'message.api_scenario_task_delete');

SET @api_scenario_execute_success_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_scenario_execute_success_id, 'SCENARIO_EXECUTE_SUCCESSFUL', 'CREATE_USER', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_scenario_execute_successful');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_execute_success_id, 'message.api_scenario_task_scenario_execute');

SET @api_scenario_fake_error_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_scenario_fake_error_id, 'SCENARIO_EXECUTE_FAKE_ERROR', 'CREATE_USER', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_scenario_execute_fake_error');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_fake_error_id, 'message.api_scenario_task_scenario_execute');

SET @api_scenario_execute_failed_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_scenario_execute_failed_id, 'SCENARIO_EXECUTE_FAILED', 'CREATE_USER', @robot_in_site_id, 'API_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_scenario_task_scenario_execute_failed');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_execute_failed_id, 'message.api_scenario_task_scenario_execute');

SET @api_scenario_report_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@api_scenario_report_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'API_REPORT_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.api_report_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@api_scenario_report_id, 'message.api_report_task_delete');

-- 初始化UI消息数据
SET @ui_update_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@ui_update_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'UI_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.ui_scenario_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@ui_update_id, 'message.ui_scenario_task_update');

SET @ui_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@ui_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'UI_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.ui_scenario_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@ui_delete_id, 'message.ui_scenario_task_delete');

SET @ui_execute_successful_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@ui_execute_successful_id, 'EXECUTE_SUCCESSFUL', 'CREATE_USER', @robot_in_site_id, 'UI_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.ui_scenario_task_execute_successful');
INSERT INTO message_task_blob(id, template) VALUES (@ui_execute_successful_id, 'message.ui_scenario_task_execute');

SET @ui_execute_failed_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@ui_execute_failed_id, 'EXECUTE_FAILED', 'CREATE_USER', @robot_in_site_id, 'UI_SCENARIO_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.ui_scenario_task_execute_failed');
INSERT INTO message_task_blob(id, template) VALUES (@ui_execute_failed_id, 'message.ui_scenario_task_execute');

SET @ui_report_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@ui_report_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'UI_REPORT_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.ui_report_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@ui_report_id, 'message.ui_report_task_delete');

-- 初始化性能测试消息数据
SET @load_create_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@load_create_id, 'UPDATE', 'CREATE_USER', @robot_in_site_id, 'LOAD_TEST_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.load_test_task_update');
INSERT INTO message_task_blob(id, template) VALUES (@load_create_id, 'message.load_test_task_update');

SET @load_delete_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@load_delete_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'LOAD_TEST_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.load_test_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@load_delete_id, 'message.load_test_task_delete');

SET @load_execute_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@load_execute_id, 'EXECUTE_COMPLETED', 'OPERATOR', @robot_in_site_id, 'LOAD_TEST_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.load_test_task_execute_completed');
INSERT INTO message_task_blob(id, template) VALUES (@load_execute_id, 'message.load_test_task_execute_completed');

SET @load_report_id = UUID_SHORT();
Insert into message_task(id, event, receiver, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@load_report_id, 'DELETE', 'CREATE_USER', @robot_in_site_id, 'LOAD_REPORT_TASK', 'NONE', '100001100001', true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.load_report_task_delete');
INSERT INTO message_task_blob(id, template) VALUES (@load_report_id, 'message.load_report_task_delete');

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

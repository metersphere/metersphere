-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化组织
INSERT INTO organization (id, num, name, description, create_user, update_user, create_time, update_time) VALUES ('default_organization', 100001, '默认组织', '系统默认创建的组织', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
-- 初始化项目
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('default_project', 100001, (SELECT id FROM organization WHERE name LIKE '默认组织'), '默认项目', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);

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
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user) VALUES (uuid(), 'admin', 'admin', 'system', 1684747668375, 'admin');

-- 初始化用户组权限
-- 系统管理员拥有所有的权限，不用初始化

-- 系统成员的权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_USER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_ORGANIZATION_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_USER_ROLE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_TEST_RESOURCE_POOL:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_PLUGIN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_PARAMETER_SETTING_BASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_PARAMETER_SETTING_DISPLAY:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_PARAMETER_SETTING_AUTH:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_AUTH:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'member', 'SYSTEM_LOG:READ');

-- 组织管理员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER_ROLE:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_MEMBER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_MEMBER:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_MEMBER:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_MEMBER:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'SYSTEM_SERVICE_INTEGRATION:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT:READ+ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT:READ+UPDATE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT:READ+DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT:READ+RECOVER');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_MEMBER:ADD');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_PROJECT_MEMBER:DELETE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_admin', 'ORGANIZATION_LOG:READ');

-- 组织成员权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_USER_ROLE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_MEMBER:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'SYSTEM_SERVICE_INTEGRATION:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_PROJECT:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (uuid(), 'org_member', 'ORGANIZATION_LOG:READ');


-- 项目管理员权限

-- 项目成员权限

-- 初始化当前站点配置
INSERT into system_parameter values('base.url', 'http://127.0.0.1:8081', 'text');
-- 初始化prometheus站点配置
INSERT into system_parameter values('base.prometheus.host', 'http://ms-prometheus:9090', 'text');

-- 初始化资源池
INSERT INTO test_resource_pool (id, name, type, description, enable, create_time, update_time, create_user, api_test, load_test, ui_test, all_org, deleted) VALUES ('a6374438-80fc-4a28-8848-96c492830af5', 'LOCAL', 'Node', '系统初始化资源池', true, 1690440108595, 1690440110182, 'admin', true, true, true, true, false);
INSERT INTO test_resource_pool_blob (id, configuration) VALUES ('a6374438-80fc-4a28-8848-96c492830af5', 0x504B03041400080808004F76FC56000000000000000000000000030000007A6970ED58C18EA33810FD1584E618266969B4D272A3136F9A9E8444408F76351921079CC413B09131D989A2FCFB96A1932604D3873DADB63941D5F3F3ABA2EC329CCC94E32424857433BC25A66D9A83ABE989E0BCB6309E9062460B69DADF4F265556898BBD254AC6880040CE05F8CCDF46A3113C659C51C905187E7FA80C316771290461D22BB3358CB0C17EFE31A8A85899A60353F23D619707863312E43826C5C572CF50DB739E843B41707245FEE4EB09D950504039109A38A7DF8828E0C136D658C6BBE1E161C5F69425B6F1CCD72B961189132CB1BD628691E235498BEAD6302424C1A280FB740A511046EEE4AC1C4A9DB23D2F1E23CF992330163989AB413916384D494A8BCC361E9445922C4FB124AF9C37B3A9EB66C69E590DE33A89BAF0A68AF1D81808B97098A4CEBD077C826C08E42F999482B26D10EF4852A670E76E19BF9AD12F12972A6F37630DC3AAB85F794322B296FF1A47405212C3ABEFF043E82AFBE8174829D4EB283A416AB23D39DA973474630C83E744603593E1321DE680D392E8A6A9A76AE7B879499EF3946F8F5F959C7D0955C708A8FA4CF970C70BA9CAA03DE86F42B73B096F7E34BAB8A07225A6B04C9A422C306719861A6C27BAD8B52D56DCB60C61DD592A3F9F6FD1841DEEF8EA620D42C70FA3D09DA3B6E22A47B6B1323F9DDE40E795D9CD33F51DEF65E6F86EF8570F5103A5657A9EA310F9918F960B3F0CA270B174C71A4A58691DE073376F850C964FC847D18B3FD352B6701A361F058B177F8CA040B44C0D8C86E5D1197F45DE249AB941883CE4F7A4AE0DD5E6EF71B10883D077965180FC6F108A56DF1D5217AB13BA8B1E69955FABA77E37D11FAEE7E8925E9134605AAED725A98DA87BC95E473FF9C89944DECBBC47C71B48ABE209394BAD04E5D4D64C15626FC5BC22FA1940E1EC9D25DB42F6BC9D4B8D7A13F4672F6113A8E59B2DA6EF2CD92B4213E3741C39B3A9AEDE600FA9FD37A3A93AA334F60377EE4CD13D6209E780254F690CDBB6BBF1B85C42C781934313588BF809DD98889B3E09079976C7B0DEF6F025B86D431D73466DE5B9E092C73C85021CDFD4CD81A76546E6BC641DCC99322FB1DCD9C650EDE86DD65A67D50B37342545DFF83A1A0B5A56370B38DA24FFB20F6D09535D985882A8C47DB4A33BDE8F76D4D6F7D18E3A747CB4231DE1FFA91DD5BBE87FA377400C120B7909CC2387B7465AABBEFDDE81AF60799C50F85E3B9DEF23EF12F9DE98862473A03EF31BBF31EA5F0109C9537EF4007DB194742A6862DAE64ECADC1E0E0BF85E65B4CCAC5DB9B6BFC0054C5B2A92F1DDEF862F03938BADABFE337CFF71FE07504B07081EC0585CBB03000038110000504B010214001400080808004F76FC561EC0585CBB030000381100000300000000000000000000000000000000007A6970504B0506000000000100010031000000EC0300000000);


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

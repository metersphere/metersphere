# 组织列表数据准备
INSERT INTO organization(id,num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('sys_default_organization_1',null, 'organization_name_1', 'XXX-1', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('sys_default_organization_2',null, 'organization_name_2', 'XXX-2', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('sys_default_organization_3',null, 'organization_name_3', 'XXX-3', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('sys_default_organization_4',null, 'organization_name_4', 'XXX-4', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id,num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('sys_default_organization_5',null, 'organization_name_5', 'XXX-5', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('sys_default_organization_6',null, 'organization_name_6', 'XXX-6', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('sys_default_organization_7',null, 'organization_name_7', 'XXX-7', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
   ('sys_default_user', 'testUserOne', 'testUserOne@metersphere.io', MD5('calong'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
    ('sys_default_user2', 'testUserTwo', 'testUserTwo@metersphere.io', MD5('calong'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
    ('sys_default_user3', 'testUserThree', 'testUserThree@metersphere.io', MD5('calong'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
    ('sys_default_user4', 'testUserFour', 'testUserFour@metersphere.io', MD5('calong'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');

INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
    ('sys_default_user5', 'testUserFive', 'testUserFive@metersphere.io', MD5('calong'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user) VALUE
    ('sys_default_user6', 'testUserSix', 'testUserSix@metersphere.io', MD5('calong'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');



INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_org_role_id_3', 'sys_default_org_role_id_3', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_3');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_project_role_id_3', 'sys_default_project_role_id_3', 'XXX', FALSE, 'PROJECT', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_3');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_org_role_id_1', 'sys_default_org_role_id_1', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_3');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_org_role_id_2', 'sys_default_org_role_id_2', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_3');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_org_role_id_4', 'sys_default_org_role_id_4', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_3');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_org_role_id_5', 'sys_default_org_role_id_5', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_3');

INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_org_role_id_7', 'sys_default_org_role_id_7', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_7');
INSERT INTO user_role(id, name, description, internal, type, create_time, update_time, create_user, scope_id) VALUE
    ('sys_default_org_role_id_8', 'sys_default_org_role_id_8', 'XXX', FALSE, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'sys_default_organization_7');

INSERT INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    ('gyq_user_role_relation_test', 'sys_default_user4', 'sys_default_org_role_id_5', 'sys_default_organization_6', 'sys_default_organization_6', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    ('gyq_user_role_relation_test-1', 'sys_default_user', 'org_admin', 'sys_default_organization_6', 'sys_default_organization_6', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    ('gyq_user_role_relation_test-2', 'admin', 'org_admin', 'sys_default_organization_3', 'sys_default_organization_3', UNIX_TIMESTAMP() * 1000, 'admin');
INSERT INTO user_role_relation(id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUE
    ('gyq_user_role_relation_test-3', 'sys_default_user4', 'sys_default_project_role_id_3', 'sys_org_projectId', 'sys_default_organization_3', UNIX_TIMESTAMP() * 1000, 'admin');
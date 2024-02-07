# 1. 准备组织, 项目, 用户数据
# 2. 准备组织成员数据
# 3. 准备项目成员数据
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('default-organization-member-test', null, 'default-organization-member-test', 'efault-organization-member-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('default-project-member-test', null, 'default-organization-member-test', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
    ('default-project-member-test-1', null, 'default-organization-member-test-1', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
    ('default-project-member-test-2', null, 'default-organization-member-test-2', '默认项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
INSERT INTO user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user, deleted) VALUES
    ('default-project-member-user-1', 'default-project-member-user1', 'project-member1@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 0),
    ('default-project-member-user-2', 'default-project-member-user2', 'project-member2@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 0),
    ('default-project-member-user-del', 'default-project-member-userDel', 'project-member-del@metersphere.io', MD5('metersphere'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', 1);

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES
    (UUID(), 'default-project-member-user-1', 'org_member', 'default-organization-member-test', 'default-organization-member-test', UNIX_TIMESTAMP() * 1000, 'admin'),
    (UUID(), 'default-project-member-user-2', 'org_member', 'default-organization-member-test', 'default-organization-member-test', UNIX_TIMESTAMP() * 1000, 'admin'),
    (UUID(), 'default-project-member-user-3', 'org_member', 'default-organization-member-test', 'default-organization-member-test', UNIX_TIMESTAMP() * 1000, 'admin'),
    (UUID(), 'default-project-member-user-1', 'org_member', 'default-organization-member-test-2', 'default-organization-member-test-2', UNIX_TIMESTAMP() * 1000, 'admin');

INSERT INTO user_role_relation (id, user_id, role_id, source_id, organization_id, create_time, create_user) VALUES
   (UUID(), 'default-project-member-user-1', 'project_admin', 'default-project-member-test', 'default-organization-member-test', UNIX_TIMESTAMP() * 1000, 'admin'),
   (UUID(), 'default-project-member-user-2', 'project_admin', 'default-project-member-test', 'default-organization-member-test', UNIX_TIMESTAMP() * 1000, 'admin'),
   (UUID(), 'default-project-member-user-del', 'project_admin', 'default-project-member-test', 'default-organization-member-test', UNIX_TIMESTAMP() * 1000, 'admin'),
    (UUID(), 'admin', 'org_admin', 'default-project-member-test-1', 'default-organization-member-test', UNIX_TIMESTAMP() * 1000, 'admin');






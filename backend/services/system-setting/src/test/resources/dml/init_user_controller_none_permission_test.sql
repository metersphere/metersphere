-- 初始化一个没有任何权限的用户
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source,
                 last_project_id, create_user, update_user)
VALUES ('tianyang.member.id', 'tianyang.member', 'tianyang.member@163.com', MD5('tianyang.member@163.com'),
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin');
INSERT INTO user_role_relation (id, user_id, role_id, source_id, create_time, create_user)
VALUES (uuid(), 'tianyang.member.id', 'member', 'system', 1684747668375, 'admin');
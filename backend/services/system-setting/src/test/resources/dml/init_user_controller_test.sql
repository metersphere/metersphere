-- 初始化一个没有任何权限的用户
insert into user(id, name, email, password, create_time, update_time, language, last_organization_id, phone, source, last_project_id, create_user, update_user, deleted)
VALUES (uuid(), 'tianyang.none.role', 'tianyang.none.role@163.com', MD5('tianyang.none.role'), UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, NULL, NUll, '', 'LOCAL', NULL, 'admin', 'admin', false);
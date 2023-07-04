# 定时删除组织列表数据准备
INSERT INTO organization(id, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('default-organization-delete2', 'default-delete2', 'XXX-delete2', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO organization(id, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('default-organization-delete1', 'default-delete1', 'XXX-delete1', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 1, 'admin', 1683464436000);
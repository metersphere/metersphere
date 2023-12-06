# 组织列表数据准备
INSERT INTO organization(id,num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('tianyang-organization-1',12138, 'tianyang-organization-1', 'XXX-1', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('tianyang-organization-2',12139, 'tianyang-organization-2', 'XXX-2', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('tianyang-organization-3',12140, 'tianyang-organization-3', 'XXX-3', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('tianyang-organization-4',12141, 'tianyang-organization-4', 'XXX-4', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id,num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('tianyang-organization-5',12142, 'tianyang-organization-5', 'XXX-5', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, delete_user, delete_time) VALUE
    ('tianyang-organization-6',12143, 'tianyang-organization-6', 'XXX-6', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, null);


INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-1', 12138, 'tianyang-organization-1', 'tianyang-projectId-1', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-2', 12139, 'tianyang-organization-1', 'tianyang-projectId-2', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-3', 12140, 'tianyang-organization-1', 'tianyang-projectId-3', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-4', 12141, 'tianyang-organization-1', 'tianyang-projectId-4', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-5', 12142, 'tianyang-organization-1', 'tianyang-projectId-5', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-6', 12143, 'tianyang-organization-1', 'tianyang-projectId-6', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-7', 12144, 'tianyang-organization-1', 'tianyang-projectId-7', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-8', 12145, 'tianyang-organization-1', 'tianyang-projectId-8', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-9', 12146, 'tianyang-organization-1', 'tianyang-projectId-9', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-10', 12147, 'tianyang-organization-1', 'tianyang-projectId-10', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-20', 12148, 'tianyang-organization-2', 'tianyang-projectId-20', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-30', 12149, 'tianyang-organization-3', 'tianyang-projectId-30', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-40', 12150, 'tianyang-organization-4', 'tianyang-projectId-40', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-50', 12151, 'tianyang-organization-5', 'tianyang-projectId-50', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
    ('tianyang-projectId-60', 12152, 'tianyang-organization-6', 'tianyang-projectId-60', '系统默认创建的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('tianyang-projectId-61', 12153, 'tianyang-organization-6', 'tianyang-projectId-60', '系统默认创建的项目',
        'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('tianyang-projectId-62', 12154, 'tianyang-organization-6', 'tianyang-projectId-60', '系统默认创建的项目',
        'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time)
VALUES ('tianyang-projectId-63', 12155, 'tianyang-organization-6', 'tianyang-projectId-60', '系统默认创建的项目',
        'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);


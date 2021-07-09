update user_role
set source_id = 'system'
where role_id = 'admin';

CREATE TABLE IF NOT EXISTS `group`
(
    `id`          VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
    `name`        VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
    `description` VARCHAR(100) COLLATE utf8mb4_bin DEFAULT NULL,
    `system`      TINYINT(1)                      NOT NULL COMMENT '是否是系统用户组',
    `type`        VARCHAR(20) COLLATE utf8mb4_bin NOT NULL COMMENT '所属类型',
    `create_time` BIGINT(13)                      NOT NULL,
    `update_time` BIGINT(13)                      NOT NULL,
    `creator`     VARCHAR(64) COLLATE utf8mb4_bin NOT NULL COMMENT '创建人(操作人）',
    `scope_id`    VARCHAR(64) COLLATE utf8mb4_bin NOT NULL COMMENT '应用范围',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `user_group`
(
    `id`          VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
    `user_id`     VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
    `group_id`    VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
    `source_id`   VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
    `create_time` BIGINT(13)                      NOT NULL,
    `update_time` BIGINT(13)                      NOT NULL,
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `user_group_permission`
(
    `id`            VARCHAR(64) COLLATE utf8mb4_bin  NOT NULL,
    `group_id`      VARCHAR(64) COLLATE utf8mb4_bin  NOT NULL COMMENT '用户组ID',
    `permission_id` VARCHAR(128) COLLATE utf8mb4_bin NOT NULL,
    `module_id`     VARCHAR(64) COLLATE utf8mb4_bin  NOT NULL COMMENT '功能菜单',
    PRIMARY KEY (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin;

-- 内置用户组
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('admin', '系统管理员(系统)', '默认用户组', 1, 'SYSTEM', 1621224000000, 1621224000000, 'admin', 'global');
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('org_admin', '组织管理员(系统)', '组织管理员', 1, 'ORGANIZATION', 1620674220001, 1620674220000, 'admin', 'global');
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('org_member', '组织成员(系统)', '组织成员', 1, 'ORGANIZATION', 1620674220002, 1620674220000, 'admin', 'global');
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('project_admin', '项目管理员(系统)', '项目管理员', 1, 'PROJECT', 1620674220004, 1620674220000, 'admin', 'global');
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('project_member', '项目成员(系统)', '项目成员', 1, 'PROJECT', 1620674220005, 1620674220000, 'admin', 'global');
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('read_only', '只读用户(系统)', '只读用户', 1, 'PROJECT', 1620674220006, 1620674220000, 'admin', 'global');
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('ws_admin', '工作空间管理员(系统)', '工作空间管理员', 1, 'WORKSPACE', 1620674220007, 1620674220000, 'admin', 'global');
INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
VALUES ('ws_member', '工作空间成员(系统)', '工作空间成员', 1, 'WORKSPACE', 1620674220008, 1620674220000, 'admin', 'global');


-- 系统管理员 组织管理员 组织成员
INSERT INTO user_group(id, user_id, group_id, source_id, create_time, update_time)
SELECT UUID(), user_id, role_id, source_id, create_time, update_time
FROM user_role
WHERE role_id IN ('admin', 'org_admin', 'org_member');

-- 测试经理
INSERT INTO user_group(id, user_id, group_id, source_id, create_time, update_time)
SELECT UUID(), user_id, 'ws_admin', source_id, create_time, update_time
FROM user_role
WHERE role_id = 'test_manager';

INSERT INTO user_group(id, user_id, group_id, source_id, create_time, update_time)
SELECT UUID(), user_id, 'project_admin', project.id, w.create_time, w.update_time
FROM project
         JOIN workspace w ON project.workspace_id = w.id
         JOIN user_role ON source_id = workspace_id
WHERE role_id = 'test_manager';

-- 测试人员
INSERT INTO user_group(id, user_id, group_id, source_id, create_time, update_time)
SELECT UUID(), user_id, 'ws_member', source_id, create_time, update_time
FROM user_role
WHERE role_id = 'test_user';

INSERT INTO user_group(id, user_id, group_id, source_id, create_time, update_time)
SELECT UUID(), user_id, 'project_member', project.id, w.create_time, w.update_time
FROM project
         JOIN workspace w ON project.workspace_id = w.id
         JOIN user_role ON source_id = workspace_id
WHERE role_id = 'test_user';

-- 只读用户
INSERT INTO user_group(id, user_id, group_id, source_id, create_time, update_time)
SELECT UUID(), user_id, 'read_only', project.id, w.create_time, w.update_time
FROM project
         JOIN workspace w ON project.workspace_id = w.id
         JOIN user_role ON source_id = workspace_id
WHERE role_id = 'test_viewer';


-- 系统管理员权限
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_GROUP:READ+CREATE', 'SYSTEM_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_USER:READ+DELETE', 'SYSTEM_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_ORGANIZATION:READ+CREATE', 'SYSTEM_ORGANIZATION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_GROUP:READ+EDIT', 'SYSTEM_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_QUOTA:READ+EDIT', 'SYSTEM_QUOTA');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_USER:READ+EDIT_PASSWORD', 'SYSTEM_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_SETTING:READ', 'SYSTEM_SETTING');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_USER:READ+EDIT', 'SYSTEM_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_USER:READ+IMPORT', 'SYSTEM_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_GROUP:READ+DELETE', 'SYSTEM_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_USER:READ', 'SYSTEM_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_SETTING:READ+EDIT', 'SYSTEM_SETTING');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_WORKSPACE:READ+EDIT', 'SYSTEM_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_WORKSPACE:READ+CREATE', 'SYSTEM_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_ORGANIZATION:READ', 'SYSTEM_ORGANIZATION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_WORKSPACE:READ+DELETE', 'SYSTEM_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_ORGANIZATION:READ+DELETE', 'SYSTEM_ORGANIZATION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_ORGANIZATION:READ+EDIT', 'SYSTEM_ORGANIZATION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_USER:READ+CREATE', 'SYSTEM_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_AUTH:READ', 'SYSTEM_AUTH');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_GROUP:READ+SETTING_PERMISSION', 'SYSTEM_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_GROUP:READ', 'SYSTEM_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_AUTH:READ+EDIT', 'SYSTEM_AUTH');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_SETTING:READ+AUTH_MANAGE', 'SYSTEM_SETTING');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_QUOTA:READ', 'SYSTEM_QUOTA');
INSERT INTO `user_group_permission` (`id`, `group_id`, `permission_id`, `module_id`)
VALUES (uuid(), 'admin', 'SYSTEM_TEST_POOL:READ+DELETE', 'SYSTEM_TEST_POOL');
INSERT `user_group_permission` (`id`, `group_id`, `permission_id`, `module_id`)
VALUES (uuid(), 'admin', 'SYSTEM_TEST_POOL:READ+CREATE', 'SYSTEM_TEST_POOL');
INSERT INTO `user_group_permission` (`id`, `group_id`, `permission_id`, `module_id`)
VALUES (uuid(), 'admin', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_TEST_POOL');
INSERT INTO `user_group_permission` (`id`, `group_id`, `permission_id`, `module_id`)
VALUES (uuid(), 'admin', 'SYSTEM_TEST_POOL:READ+EDIT', 'SYSTEM_TEST_POOL');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'admin', 'SYSTEM_OPERATING_LOG:READ', 'SYSTEM_OPERATING_LOG');


-- 组织管理员
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ+DELETE', 'ORGANIZATION_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ', 'ORGANIZATION_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_WORKSPACE:READ+EDIT', 'ORGANIZATION_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'PROJECT_TRACK_REVIEW:READ+CREATE', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_WORKSPACE:READ', 'ORGANIZATION_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_WORKSPACE:READ+CREATE', 'ORGANIZATION_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_GROUP:READ', 'ORGANIZATION_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ+EDIT', 'ORGANIZATION_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_MESSAGE:READ', 'ORGANIZATION_MESSAGE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_USER:READ+CREATE', 'ORGANIZATION_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_GROUP:READ+CREATE', 'ORGANIZATION_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_GROUP:READ+EDIT', 'ORGANIZATION_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_WORKSPACE:READ+DELETE', 'ORGANIZATION_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_GROUP:READ+SETTING_PERMISSION', 'ORGANIZATION_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_SERVICE:READ', 'ORGANIZATION_SERVICE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_MESSAGE:READ+EDIT', 'ORGANIZATION_MESSAGE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_SERVICE:READ+EDIT', 'ORGANIZATION_SERVICE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_admin', 'ORGANIZATION_GROUP:READ+DELETE', 'ORGANIZATION_GROUP');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'org_admin', 'ORGANIZATION_OPERATING_LOG:READ', 'ORGANIZATION_OPERATING_LOG');

-- 组织成员
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_member', 'ORGANIZATION_WORKSPACE:READ', 'ORGANIZATION_WORKSPACE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_member', 'ORGANIZATION_SERVICE:READ', 'ORGANIZATION_SERVICE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_member', 'ORGANIZATION_GROUP:READ', 'ORGANIZATION_GROUP');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_member', 'ORGANIZATION_MESSAGE:READ', 'ORGANIZATION_MESSAGE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'org_member', 'ORGANIZATION_USER:READ', 'ORGANIZATION_USER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'org_member', 'ORGANIZATION_OPERATING_LOG:READ', 'ORGANIZATION_OPERATING_LOG');

-- 工作空间管理员
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_USER:READ+CREATE', 'WORKSPACE_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_TEMPLATE:READ+CUSTOM', 'WORKSPACE_TEMPLATE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_TEMPLATE:READ+REPORT_TEMPLATE', 'WORKSPACE_TEMPLATE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_USER:READ', 'WORKSPACE_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_USER:READ+DELETE', 'WORKSPACE_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_USER:READ+EDIT', 'WORKSPACE_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_TEMPLATE:READ', 'WORKSPACE_TEMPLATE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_TEMPLATE:READ+ISSUE_TEMPLATE', 'WORKSPACE_TEMPLATE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_TEMPLATE:READ+CASE_TEMPLATE', 'WORKSPACE_TEMPLATE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+CREATE', 'WORKSPACE_PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+DELETE', 'WORKSPACE_PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_MANAGER:READ+EDIT', 'WORKSPACE_PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+COPY', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+CREATE', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+IMPORT', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+EXPORT', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+EDIT', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_PROJECT_ENVIRONMENT:READ+DELETE', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_admin', 'WORKSPACE_OPERATING_LOG:READ', 'WORKSPACE_OPERATING_LOG');


-- 工作空间成员
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_member', 'WORKSPACE_USER:READ', 'WORKSPACE_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_member', 'WORKSPACE_TEMPLATE:READ', 'WORKSPACE_TEMPLATE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_member', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_member', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'ws_member', 'WORKSPACE_OPERATING_LOG:READ', 'WORKSPACE_OPERATING_LOG');

-- 项目管理员
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_USER:READ+CREATE', 'PROJECT_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_TEST:READ+SCHEDULE', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+EDIT', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_TEST:READ+COPY', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_TEST:READ+EDIT', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+DEBUG', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+MOVE_BATCH', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+MOCK', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+EDIT_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+DELETE_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_TEST:READ', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_REVIEW:READ', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_MANAGER:READ+EDIT', 'PROJECT_MANAGER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_REPORT:READ+DELETE', 'PROJECT_PERFORMANCE_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+CREATE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+EXPORT', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_REVIEW:READ+COMMENT', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+DEBUG', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_REVIEW:READ+DELETE', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_TEST:READ+RUN', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_USER:READ', 'PROJECT_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+COPY_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_CASE:READ+EXPORT', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+CREATE', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+EXPORT_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+IMPORT_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+EDIT', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_TEST:READ+CREATE', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+EDIT', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+COPY', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+EXPORT_SCENARIO', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+IMPORT', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_REVIEW:READ+EDIT', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_REVIEW:READ+RELEVANCE_OR_CANCEL', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+DELETE_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+CREATE_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_TEST:READ+DELETE', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_REVIEW:READ+REVIEW', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+SCHEDULE', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_MANAGER:READ', 'PROJECT_MANAGER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_CASE:READ+DELETE', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_REVIEW:READ+CREATE', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+RUN', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+DELETE', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_CASE:READ', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+IMPORT_SCENARIO', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_REPORT:READ', 'PROJECT_API_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_CASE:READ+IMPORT', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+CREATE_PERFORMANCE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_USER:READ+EDIT', 'PROJECT_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_USER:READ+DELETE', 'PROJECT_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+EDIT_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_CASE:READ+CREATE', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+DELETE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+CREATE_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_REPORT:READ+DELETE', 'PROJECT_API_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_REPORT:READ', 'PROJECT_PERFORMANCE_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_CASE:READ+EDIT', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+CREATE_PERFORMANCE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+SCHEDULE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_SCENARIO:READ+COPY', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+RUN', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_admin', 'PROJECT_TRACK_CASE:READ+COPY', 'PROJECT_TRACK_CASE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_OPERATING_LOG');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+CASE_BATCH_EDIT', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+CASE_BATCH_RUN', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+REPORT_DELETE', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+CREATE', 'PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_ENVIRONMENT:READ+DELETE', 'PROJECT_ENVIRONMENT');


-- 项目成员
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+DELETE_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_MANAGER:READ', 'PROJECT_MANAGER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+CREATE_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_CASE:READ+IMPORT', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+EDIT', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+CREATE', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+IMPORT_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+EXPORT_SCENARIO', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_ENVIRONMENT:READ+IMPORT', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_CASE:READ+CREATE', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_REVIEW:READ+DELETE', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+IMPORT_SCENARIO', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+DELETE', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_CASE:READ+DELETE', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+CREATE_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+MOCK', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+DELETE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_ENVIRONMENT:READ+COPY', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+RUN', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+MOVE_BATCH', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_CASE:READ+EXPORT', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_REPORT:READ', 'PROJECT_PERFORMANCE_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_TEST:READ', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_CASE:READ+EDIT', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_TEST:READ+CREATE', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_USER:READ+DELETE', 'PROJECT_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+SCHEDULE', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+EXPORT_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_ENVIRONMENT:READ+EDIT', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_REPORT:READ+DELETE', 'PROJECT_PERFORMANCE_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_MANAGER:READ+EDIT', 'PROJECT_MANAGER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_REVIEW:READ', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_TEST:READ+COPY', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+DEBUG', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_REPORT:READ+DELETE', 'PROJECT_API_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+EDIT_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+CREATE_PERFORMANCE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_REVIEW:READ+EDIT', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+COPY_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+COPY', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_REPORT:READ', 'PROJECT_API_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_REVIEW:READ+REVIEW', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_ENVIRONMENT:READ+EXPORT', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+SCHEDULE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_TEST:READ+EDIT', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_TEST:READ+DELETE', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_CASE:READ', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+CREATE_PERFORMANCE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_USER:READ', 'PROJECT_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+DELETE_API', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_REVIEW:READ+COMMENT', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_CASE:READ+COPY', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_REVIEW:READ+CREATE', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_TEST:READ+SCHEDULE', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+EDIT', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+CREATE', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_REVIEW:READ+RELEVANCE_OR_CANCEL', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_PERFORMANCE_TEST:READ+RUN', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+DEBUG', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+EDIT_CASE', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_API_SCENARIO:READ+RUN', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_OPERATING_LOG');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+CASE_BATCH_EDIT', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+CASE_BATCH_RUN', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+REPORT_DELETE', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_ENVIRONMENT:READ+CREATE', 'PROJECT_ENVIRONMENT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_ENVIRONMENT:READ+DELETE', 'PROJECT_ENVIRONMENT');

-- 只读用户
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_API_DEFINITION:READ', 'PROJECT_API_DEFINITION');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_PERFORMANCE_TEST:READ', 'PROJECT_PERFORMANCE_TEST');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_TRACK_REVIEW:READ', 'PROJECT_TRACK_REVIEW');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_TRACK_PLAN:READ', 'PROJECT_TRACK_PLAN');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_TRACK_CASE:READ', 'PROJECT_TRACK_CASE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_PERFORMANCE_REPORT:READ', 'PROJECT_PERFORMANCE_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_API_SCENARIO:READ', 'PROJECT_API_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_ENVIRONMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_API_REPORT:READ', 'PROJECT_API_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_USER:READ', 'PROJECT_USER');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'read_only', 'PROJECT_MANAGER:READ', 'PROJECT_MANAGER');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'read_only', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_OPERATING_LOG');
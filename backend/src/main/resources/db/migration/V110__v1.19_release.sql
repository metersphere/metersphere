INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_REPOSITORY:READ', 'WORKSPACE_USER');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_member', 'WORKSPACE_REPOSITORY:READ', 'WORKSPACE_USER');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_REPOSITORY:READ+CREATE', 'WORKSPACE_USER');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_REPOSITORY:READ+EDIT', 'WORKSPACE_USER');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_REPOSITORY:READ+DELETE', 'WORKSPACE_USER');

DROP TABLE IF EXISTS `workspace_repository`;
CREATE TABLE IF NOT EXISTS `workspace_repository`  
(
     `id` varchar(50) NOT NULL COMMENT 'Repository ID',
     `repository_name` varchar(100) NOT NULL COMMENT '存储库名称',
     `repository_url` varchar(300)  NOT NULL COMMENT '存储库地址',
     `username` varchar(256)  NOT NULL COMMENT 'UserName',
     `password` varchar(256)  NOT NULL COMMENT 'Password',
     `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
     `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
     `workspace_id` varchar(50) DEFAULT NULL COMMENT '工作空间ID',
     `create_user` varchar(100) DEFAULT NULL COMMENT '创建人',
     `description` longtext COMMENT '仓库描述信息',
      PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `workspace_repository_file_version`;
CREATE TABLE `workspace_repository_file_version` (
     `id` varchar(50) NOT NULL COMMENT 'ID',
     `repository_id` varchar(50) NOT NULL COMMENT '存储库ID',
     `branch` varchar(100) NOT NULL COMMENT '存储库分支',
     `path` varchar(500) NOT NULL COMMENT '文件路径',
     `scenario_id` varchar(100) NOT NULL COMMENT '场景ID',
     `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
     `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
     `create_user` varchar(100) DEFAULT NULL COMMENT '创建人',
     `commit_id` varchar(100) NOT NULL COMMENT '文件commentId',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
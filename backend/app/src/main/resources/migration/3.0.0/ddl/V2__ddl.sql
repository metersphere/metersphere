CREATE TABLE IF NOT EXISTS `project`
(
    `id`           VARCHAR(50) NOT NULL COMMENT '项目ID',
    `workspace_id` VARCHAR(50) NOT NULL COMMENT '工作空间ID',
    `name`         VARCHAR(64) NOT NULL COMMENT '项目名称',
    `description`  VARCHAR(255) COMMENT '项目描述',
    `create_time`  BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`  BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`  VARCHAR(100) COMMENT '创建人',
    `system_id`    VARCHAR(50) COMMENT '',
    PRIMARY KEY (id)
) COMMENT = '项目';


CREATE TABLE IF NOT EXISTS `user`
(
    `id`                VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `name`              VARCHAR(64)  NOT NULL COMMENT '用户名',
    `email`             VARCHAR(64)  NOT NULL COMMENT '用户邮箱',
    `password`          VARCHAR(256) COMMENT '用户密码',
    `status`            VARCHAR(50)  NOT NULL COMMENT '用户状态，启用或禁用',
    `create_time`       BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`       BIGINT       NOT NULL COMMENT '更新时间',
    `language`          VARCHAR(30) COMMENT '语言',
    `last_workspace_id` VARCHAR(50) COMMENT '当前工作空间ID',
    `phone`             VARCHAR(50) COMMENT '手机号',
    `source`            VARCHAR(50)  NOT NULL COMMENT '来源：LOCAL OIDC CAS',
    `last_project_id`   VARCHAR(50) COMMENT '当前项目ID',
    `create_user`       VARCHAR(100) NOT NULL COMMENT '创建人',
    PRIMARY KEY (id)
) COMMENT = '用户';

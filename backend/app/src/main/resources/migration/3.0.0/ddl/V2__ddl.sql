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

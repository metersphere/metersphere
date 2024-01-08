-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE test_plan_module
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `project_id`  VARCHAR(50) NOT NULL COMMENT '项目名称',
    `name`        VARCHAR(64) NOT NULL COMMENT '模块名称',
    `parent_id`   VARCHAR(50) NOT NULL COMMENT '模块ID',
    `pos`         BIGINT      NOT NULL COMMENT '排序标识',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(100) COMMENT '创建人',
    `update_user` VARCHAR(100) COMMENT '更新人',
    PRIMARY KEY (id)
) COMMENT = '测试计划模块';


CREATE INDEX idx_project_id ON test_plan_module (project_id);
CREATE INDEX idx_name ON test_plan_module (name);
CREATE INDEX idx_pos ON test_plan_module (pos);
CREATE UNIQUE INDEX uq_name_project_parent ON test_plan_module (project_id, name, parent_id);

CREATE TABLE IF NOT EXISTS test_plan
(
    `id`                 VARCHAR(50)  NOT NULL COMMENT 'ID',
    `project_id`         VARCHAR(50)  NOT NULL COMMENT '测试计划所属项目',
    `parent_id`          VARCHAR(50)  NOT NULL COMMENT '测试计划父ID;测试计划要改为树结构。最上层的为root，其余则是父节点ID',
    `name`               VARCHAR(255) NOT NULL COMMENT '测试计划名称',
    `status`             VARCHAR(20)  NOT NULL COMMENT '测试计划状态;进行中/未开始/已完成/已结束/已归档',
    `stage`              VARCHAR(30)  NOT NULL COMMENT '测试阶段',
    `tags`               VARCHAR(500) COMMENT '标签',
    `create_time`        BIGINT       NOT NULL COMMENT '创建时间',
    `create_user`        VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_time`        BIGINT COMMENT '更新时间',
    `update_user`        VARCHAR(50) COMMENT '更新人',
    `planned_start_time` BIGINT COMMENT '计划开始时间',
    `planned_end_time`   BIGINT COMMENT '计划结束时间',
    `actual_start_time`  BIGINT COMMENT '实际开始时间',
    `actual_end_time`    BIGINT COMMENT '实际结束时间',
    `description`        VARCHAR(2000) COMMENT '描述',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '测试计划';


CREATE INDEX idx_parent_id ON test_plan (project_id);
CREATE INDEX idx_create_user ON test_plan (create_user);
CREATE INDEX idx_status ON test_plan (status);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
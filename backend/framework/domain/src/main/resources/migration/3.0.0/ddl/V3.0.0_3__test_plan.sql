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


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
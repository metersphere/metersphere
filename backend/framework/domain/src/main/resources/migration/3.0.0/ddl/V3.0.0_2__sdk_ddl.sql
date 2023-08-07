-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS operation_log
(
    `id`          VARCHAR(50)  NOT NULL COMMENT '主键',
    `project_id`  VARCHAR(50)  NOT NULL DEFAULT 'NONE' COMMENT '项目id',
    `organization_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '组织id',
    `create_time` BIGINT       NOT NULL COMMENT '操作时间',
    `create_user` VARCHAR(50) COMMENT '操作人',
    `source_id`   VARCHAR(50) COMMENT '资源id',
    `method`      VARCHAR(255) NOT NULL COMMENT '操作方法',
    `type`        VARCHAR(20)  NOT NULL COMMENT '操作类型/add/update/delete',
    `module`      VARCHAR(50) COMMENT '操作模块/api/case/scenario/ui',
    `content`     VARCHAR(500) COMMENT '操作详情',
    `path`        VARCHAR(255) COMMENT '操作路径',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '操作日志';

CREATE INDEX idx_create_time ON operation_log(create_time);
CREATE INDEX idx_create_user ON operation_log(create_user);
CREATE INDEX idx_method ON operation_log(method);
CREATE INDEX idx_module ON operation_log(module);
CREATE INDEX idx_project_id ON operation_log(project_id);
CREATE INDEX idx_type ON operation_log(type);
CREATE INDEX idx_organization_id ON operation_log(organization_id);
CREATE INDEX idx_source_id ON operation_log(source_id);


DROP TABLE IF EXISTS operation_log_blob;
CREATE TABLE operation_log_blob(
                                   `id` VARCHAR(50) NOT NULL   COMMENT '主键' ,
                                   `original_value` LONGBLOB    COMMENT '变更前内容' ,
                                   `modified_value` LONGBLOB    COMMENT '变更后内容' ,
                                   PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '操作日志内容详情';
-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
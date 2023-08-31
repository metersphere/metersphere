-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS operation_log
(
    `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键' ,
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
   `id` BIGINT NOT NULL  COMMENT '主键,与operation_log表id一致' ,
   `original_value` LONGBLOB    COMMENT '变更前内容' ,
   `modified_value` LONGBLOB    COMMENT '变更后内容' ,
   PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '操作日志内容详情';


CREATE TABLE IF NOT EXISTS environment(
    `id` VARCHAR(50) NOT NULL   COMMENT '环境ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '环境名称' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '环境';

CREATE INDEX idx_project_id ON environment(project_id);
CREATE INDEX idx_name ON environment(name);
CREATE INDEX idx_create_time ON environment(create_time);

CREATE TABLE IF NOT EXISTS environment_blob(
 `id` VARCHAR(50) NOT NULL   COMMENT '环境ID' ,
 `config` LONGBLOB NOT NULL   COMMENT 'Config Data (JSON format)' ,
 PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '环境配置';

CREATE TABLE IF NOT EXISTS environment_group(
  `id` VARCHAR(50) NOT NULL   COMMENT '环境组id' ,
  `name` VARCHAR(255) NOT NULL   COMMENT '环境组名' ,
  `project_id` VARCHAR(50) NOT NULL   COMMENT '所属项目id' ,
  `description` VARCHAR(500)    COMMENT '环境组描述' ,
  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
  `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
  `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
  PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '环境组';

CREATE INDEX idx_name ON environment_group(name);
CREATE INDEX idx_project_id ON environment_group(project_id);
CREATE INDEX idx_create_user ON environment_group(create_user);
CREATE INDEX idx_create_time ON environment_group(create_time);
CREATE INDEX idx_update_time ON environment_group(update_time);

CREATE TABLE IF NOT EXISTS environment_group_relation(
   `id` VARCHAR(50) NOT NULL   COMMENT '' ,
   `environment_group_id` VARCHAR(50) NOT NULL   COMMENT '环境组id' ,
   `environment_id` VARCHAR(50) NOT NULL   COMMENT '环境ID' ,
   `project_id` VARCHAR(50) NOT NULL   COMMENT '项目id' ,
   PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '环境组关联关系';

CREATE INDEX idx_environment_group_id ON environment_group_relation(environment_group_id);
CREATE INDEX idx_environment_id ON environment_group_relation(environment_id);
CREATE INDEX idx_project_id ON environment_group_relation(project_id);

CREATE TABLE IF NOT EXISTS project_parameters(
   `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
   `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
   `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
   `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
   `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
   `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
   `parameters` LONGBLOB    COMMENT '参数配置' ,
   PRIMARY KEY (id)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '项目级参数';

CREATE INDEX idx_project_id ON project_parameters(project_id);



-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

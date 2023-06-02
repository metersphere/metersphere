-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS api_definition(
                               `id` VARCHAR(50) NOT NULL   COMMENT '接口pk' ,
                               `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                               `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                               `update_time` BIGINT NOT NULL   COMMENT '修改时间' ,
                               `update_user` VARCHAR(50) NOT NULL   COMMENT '修改人' ,
                               `delete_user` VARCHAR(50)    COMMENT '删除人' ,
                               `delete_time` BIGINT    COMMENT '删除时间' ,
                               `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '删除状态' ,
                               `name` VARCHAR(255) NOT NULL   COMMENT '接口名称' ,
                               `method` VARCHAR(50) NOT NULL   COMMENT '接口类型' ,
                               `protocol` VARCHAR(20) NOT NULL   COMMENT '接口协议' ,
                               `path` VARCHAR(255)   DEFAULT '' COMMENT '接口路径-只有HTTP协议有值' ,
                               `module_path` VARCHAR(1000)    COMMENT '模块全路径-用于导入处理' ,
                               `status` VARCHAR(50) NOT NULL   COMMENT '接口状态/进行中/已完成' ,
                               `module_id` VARCHAR(50)   DEFAULT 'NONE' COMMENT '模块fk' ,
                               `num` INT    COMMENT '自定义id' ,
                               `tags` VARCHAR(1000)    COMMENT '标签' ,
                               `pos` BIGINT NOT NULL   COMMENT '自定义排序' ,
                               `sync_enable` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否启用同步' ,
                               `sync_time` BIGINT    COMMENT '同步开始时间' ,
                               `project_id` VARCHAR(50) NOT NULL   COMMENT '项目fk' ,
                               `environment_id` VARCHAR(50)    COMMENT '环境fk' ,
                               `latest` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是' ,
                               `version_id` VARCHAR(50)    COMMENT '版本fk' ,
                               `ref_id` VARCHAR(50)    COMMENT '版本引用fk' ,
                               `description` VARCHAR(500)    COMMENT '描述' ,
                               PRIMARY KEY (id)
)  COMMENT = '接口定义';


CREATE INDEX idx_project_id ON api_definition(project_id);
CREATE INDEX idx_module_id ON api_definition(module_id);
CREATE INDEX idx_ref_id ON api_definition(ref_id);
CREATE INDEX idx_version_id ON api_definition(version_id);
CREATE INDEX idx_method ON api_definition(method);
CREATE INDEX idx_status ON api_definition(status);
CREATE INDEX idx_pos ON api_definition(pos);
CREATE INDEX idx_protocol ON api_definition(protocol);
CREATE INDEX idx_create_time ON api_definition(create_time);
CREATE INDEX idx_create_user ON api_definition(create_user);
CREATE INDEX idx_name ON api_definition(name);
CREATE INDEX idx_path ON api_definition(path);

CREATE TABLE IF NOT EXISTS api_definition_blob(
                                    `id` VARCHAR(50) NOT NULL   COMMENT '接口fk/ 一对一关系' ,
                                    `request` LONGBLOB    COMMENT '请求内容' ,
                                    `response` LONGBLOB    COMMENT '响应内容' ,
                                    `remark` BLOB    COMMENT '备注' ,
                                    PRIMARY KEY (id)
)  COMMENT = '接口定义详情内容';



-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
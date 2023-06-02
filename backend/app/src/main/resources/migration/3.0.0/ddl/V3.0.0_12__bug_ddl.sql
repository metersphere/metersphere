-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

DROP TABLE IF EXISTS bug;
CREATE TABLE IF NOT EXISTS bug(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `num` INT NOT NULL   COMMENT '业务ID' ,
    `title` VARCHAR(300) NOT NULL   COMMENT '缺陷标题' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `platform` VARCHAR(50) NOT NULL   COMMENT '缺陷平台' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `source_id` VARCHAR(50)    COMMENT '缺陷来源，记录创建该缺陷的测试计划的ID' ,
    `platform_status` VARCHAR(50) NOT NULL  DEFAULT '' COMMENT '第三方平台状态' ,
    `platform_id` VARCHAR(50)    COMMENT '第三方平台缺陷ID' ,
    PRIMARY KEY (id)
    )  COMMENT = '缺陷';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

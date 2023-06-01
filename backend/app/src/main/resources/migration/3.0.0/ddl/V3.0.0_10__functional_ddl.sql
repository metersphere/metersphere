-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS case_review(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `name` VARCHAR(200) NOT NULL   COMMENT '名称' ,
    `status` VARCHAR(64) NOT NULL  DEFAULT 'Prepare' COMMENT '评审状态：未开始/进行中/已完成/已结束/已归档' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `end_time` BIGINT NOT NULL   COMMENT '评审结束时间' ,
    `description` TEXT    COMMENT '描述' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `tags` VARCHAR(1000)    COMMENT '标签' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `review_pass_rule` VARCHAR(64) NOT NULL  DEFAULT 'Single' COMMENT '评审规则：单人通过/全部通过' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '用例评审';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
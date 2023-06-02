-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS test_plan(
                          `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                          `project_id` VARCHAR(50) NOT NULL   COMMENT '测试计划所属项目' ,
                          `parent_id` VARCHAR(50) NOT NULL   COMMENT '测试计划父ID;测试计划要改为树结构。最上层的为root，其余则是父节点ID' ,
                          `name` VARCHAR(255) NOT NULL   COMMENT '测试计划名称' ,
                          `status` VARCHAR(20) NOT NULL   COMMENT '测试计划状态;进行中/未开始/已完成/已结束/已归档' ,
                          `stage` VARCHAR(30) NOT NULL   COMMENT '测试阶段' ,
                          `tags` VARCHAR(500)    COMMENT '标签' ,
                          `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                          `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                          `update_time` BIGINT    COMMENT '更新时间' ,
                          `update_user` VARCHAR(50)    COMMENT '更新人' ,
                          `planned_start_time` BIGINT    COMMENT '计划开始时间' ,
                          `planned_end_time` BIGINT    COMMENT '计划结束时间' ,
                          `actual_start_time` BIGINT    COMMENT '实际开始时间' ,
                          `actual_end_time` BIGINT    COMMENT '实际结束时间' ,
                          `description` VARCHAR(2000)    COMMENT '描述' ,
                          PRIMARY KEY (id)
)  COMMENT = '测试计划';


CREATE INDEX idx_parent_id ON test_plan(project_id);
CREATE INDEX idx_project_id ON test_plan(project_id);
CREATE INDEX idx_create_user ON test_plan(create_user);
CREATE INDEX idx_status ON test_plan(status);

CREATE TABLE IF NOT EXISTS test_plan_follower(
                                   `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID;联合主键' ,
                                   `user_id` VARCHAR(50) NOT NULL   COMMENT '用户ID;联合主键' ,
                                   PRIMARY KEY (test_plan_id,user_id)
)  COMMENT = '测试计划关注人';

CREATE TABLE IF NOT EXISTS test_plan_principal(
                                    `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID' ,
                                    `user_id` VARCHAR(50) NOT NULL   COMMENT '用户ID' ,
                                    PRIMARY KEY (test_plan_id,user_id)
)  COMMENT = '测试计划责任人';

CREATE TABLE IF NOT EXISTS test_plan_config(
                                 `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID' ,
                                 `run_mode_config` TEXT NOT NULL   COMMENT '运行模式' ,
                                 `automatic_status_update` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否自定更新功能用例状态' ,
                                 `repeat_case` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否允许重复添加用例' ,
                                 `pass_threshold` INT(3) NOT NULL  DEFAULT 100 COMMENT '测试计划通过阈值;0-100' ,
                                 PRIMARY KEY (test_plan_id)
)  COMMENT = '测试计划配置';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
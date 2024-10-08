-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 执行任务表
CREATE TABLE IF NOT EXISTS exec_task(
    `id` VARCHAR(50) NOT NULL   COMMENT '任务ID' ,
    `num` BIGINT NOT NULL   COMMENT '编号' ,
    `task_name` VARCHAR(255) NOT NULL   COMMENT '任务名称' ,
    `status` VARCHAR(20) NOT NULL   COMMENT '状态' ,
    `case_count` BIGINT NOT NULL   COMMENT '用例数量' ,
    `result` VARCHAR(64)    COMMENT '执行结果' ,
    `task_type` VARCHAR(50) NOT NULL   COMMENT '任务类型' ,
    `trigger_mode` VARCHAR(20) NOT NULL   COMMENT '执行模式' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `organization_id` VARCHAR(50) NOT NULL   COMMENT '组织ID' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `start_time` BIGINT    COMMENT '开始时间' ,
    `end_time` BIGINT    COMMENT '结束时间' ,
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '执行任务表';

CREATE INDEX idx_num ON exec_task(num);
CREATE INDEX idx_name ON exec_task(task_name);
CREATE INDEX idx_status ON exec_task(status);
CREATE INDEX idx_result ON exec_task(result);
CREATE INDEX idx_project_id ON exec_task(project_id);
CREATE INDEX idx_organization_id ON exec_task(organization_id);
CREATE INDEX idx_create_time ON exec_task(create_time desc);
CREATE INDEX idx_create_user ON exec_task(create_user);
CREATE INDEX idx_start_time ON exec_task(start_time);
CREATE INDEX idx_end_time ON exec_task(end_time);
CREATE INDEX idx_trigger_mode ON exec_task(trigger_mode);


-- 执行任务详情表
CREATE TABLE IF NOT EXISTS exec_task_item(
    `id` VARCHAR(50) NOT NULL   COMMENT '主键ID' ,
    `task_id` VARCHAR(50) NOT NULL   COMMENT '任务ID' ,
    `resource_id` VARCHAR(50) NOT NULL   COMMENT '资源ID' ,
    `task_origin` VARCHAR(50)    COMMENT '任务来源（任务组下的任务id）' ,
    `status` VARCHAR(20) NOT NULL   COMMENT '执行状态' ,
    `result` VARCHAR(255)    COMMENT '执行结果' ,
    `resource_pool_id` VARCHAR(50) NOT NULL   COMMENT '资源池ID' ,
    `resource_pool_node` VARCHAR(50)    COMMENT '节点' ,
    `resource_type` VARCHAR(50) NOT NULL   COMMENT '资源类型' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `organization_id` VARCHAR(50) NOT NULL   COMMENT '组织ID' ,
    `thread_id` VARCHAR(50)    COMMENT '线程ID' ,
    `start_time` BIGINT    COMMENT '执行开始时间' ,
    `end_time` BIGINT    COMMENT '执行完成时间' ,
    `executor` VARCHAR(50) NOT NULL   COMMENT '执行人' ,
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '执行任务详情表';

CREATE INDEX idx_task_id ON exec_task_item(task_id);
CREATE INDEX idx_resource_id ON exec_task_item(resource_id);
CREATE INDEX idx_status ON exec_task_item(status);
CREATE INDEX idx_result ON exec_task_item(result);
CREATE INDEX idx_resource_pool_id ON exec_task_item(resource_pool_id);
CREATE INDEX idx_resource_pool_node ON exec_task_item(resource_pool_node);
CREATE INDEX idx_project_id ON exec_task_item(project_id);
CREATE INDEX idx_organization_id ON exec_task_item(organization_id);
CREATE INDEX idx_thread_id ON exec_task_item(thread_id);
CREATE INDEX idx_start_time ON exec_task_item(start_time desc);
CREATE INDEX idx_end_time ON exec_task_item(end_time desc);
CREATE INDEX idx_executor ON exec_task_item(executor);


ALTER TABLE project
    ADD all_resource_pool BIT DEFAULT b'0' NOT NULL COMMENT '全部资源池';

CREATE INDEX idx_all_resource_pool ON project(all_resource_pool);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
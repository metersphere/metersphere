-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 缺陷自定义字段增加文本字段
ALTER TABLE bug_custom_field ADD COLUMN `content` longtext COMMENT '字段文本';


CREATE TABLE IF NOT EXISTS user_layout(
                            `id` VARCHAR(50) NOT NULL   COMMENT 'id' ,
                            `user_id` VARCHAR(50) NOT NULL   COMMENT '用户ID' ,
                            `org_id` VARCHAR(50) NOT NULL   COMMENT '组织ID' ,
                            `configuration` LONGBLOB    COMMENT '用户布局配置字段' ,
                            PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户布局表';

CREATE INDEX idx_user_id_org_id ON user_layout(`user_id`,`org_id`);


CREATE INDEX idx_project_id_delete_create_time_create_user
    ON functional_case (project_id, deleted, create_time, create_user);

CREATE INDEX idx_project_id_delete_create_time
    ON functional_case (project_id, deleted, create_time);


DROP INDEX idx_create_user ON case_review;

CREATE INDEX idx_project_id_create_time
    ON case_review (project_id, create_time);

CREATE INDEX idx_project_id_create_time_create_user
    ON case_review (project_id, create_time, create_user);

DROP INDEX idx_create_user ON api_definition;

CREATE INDEX idx_project_id_delete_create_time_create_user
    ON api_definition (project_id, deleted, create_time, create_user);

CREATE INDEX idx_project_id_delete_create_time
    ON api_definition (project_id, deleted, create_time);

DROP INDEX idx_create_user ON api_test_case;

CREATE INDEX idx_project_id_delete_create_time
    ON api_test_case (project_id, deleted, create_time);


CREATE INDEX idx_project_id_delete_create_time_create_user
    ON api_test_case (project_id, deleted, create_time, create_user);

DROP INDEX idx_create_user ON api_scenario;

CREATE INDEX idx_project_id_delete_create_time
    ON api_scenario (project_id, deleted, create_time);

CREATE INDEX idx_project_id_delete_create_time_create_user
    ON api_scenario (project_id, deleted, create_time, create_user);

DROP INDEX idx_create_user ON test_plan;

CREATE INDEX idx_project_id_delete_create_time
    ON test_plan (project_id, create_time);

CREATE INDEX idx_project_id_create_time_create_user
    ON test_plan (project_id, create_time, create_user);

CREATE INDEX idx_project_id_delete_create_time
    ON bug (project_id, deleted, create_time);

CREATE INDEX idx_project_id_delete_create_time_create_user
    ON bug (project_id, deleted, create_time, create_user);

create index idx_test_plan_id_bug_id
    on bug_relation_case (test_plan_id, bug_id);

ALTER TABLE exec_task ADD COLUMN deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '删除标识';
ALTER TABLE exec_task_item ADD COLUMN deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '删除标识';
ALTER TABLE exec_task_item ADD COLUMN case_id VARCHAR(50) COMMENT '用例表id';
CREATE INDEX idx_case_id ON exec_task_item(case_id);
-- 任务项添加测试集字段
ALTER TABLE exec_task_item ADD collection_id varchar(50) NULL COMMENT '测试集ID';
-- 任务项添加异常信息字段
ALTER TABLE exec_task_item ADD error_message varchar(50) NULL COMMENT '异常信息';
-- 任务项添加重跑字段
ALTER TABLE exec_task_item ADD rerun bit(1) DEFAULT 0 NULL COMMENT '是否是重跑任务项';
-- 任务项添加创建时间字段
ALTER TABLE exec_task_item ADD create_time bigint NOT NULL COMMENT '创建时间';
-- 任务添加串并行字段
ALTER TABLE exec_task ADD `parallel` bit(1) DEFAULT 1 NOT NULL COMMENT '是否是并行执行';

-- 任务记录批量执行的环境等信息
ALTER TABLE exec_task ADD environment_id varchar(50) NULL COMMENT '用例批量执行环境ID';
ALTER TABLE exec_task ADD env_grouped bit(1) DEFAULT 0 NULL COMMENT '是否是环境组';
ALTER TABLE exec_task ADD pool_id varchar(50) NULL COMMENT '资源池ID';

-- 执行状态字段添加默认值
ALTER TABLE test_plan_functional_case ALTER COLUMN last_exec_result SET DEFAULT 'PENDING';
ALTER TABLE test_plan_api_case ALTER COLUMN last_exec_result SET DEFAULT '';
ALTER TABLE test_plan_api_scenario ALTER COLUMN last_exec_result SET DEFAULT '';
ALTER TABLE api_test_case ALTER COLUMN last_report_status SET DEFAULT '';
ALTER TABLE api_scenario ALTER COLUMN last_report_status SET DEFAULT '';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 缺陷自定义字段增加文本字段
ALTER TABLE bug_custom_field ADD COLUMN `content` VARCHAR(1000) COMMENT '字段文本';


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

ALTER TABLE exec_task ADD COLUMN deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '删除标识';
ALTER TABLE exec_task_item ADD COLUMN deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '删除标识';
ALTER TABLE exec_task_item ADD COLUMN case_id VARCHAR(50) COMMENT '用例表id';
CREATE INDEX idx_case_id ON exec_task_item(case_id);
-- 任务项添加测试集字段
ALTER TABLE exec_task_item ADD collection_id varchar(50) NULL COMMENT '测试集ID';
-- 任务项添加异常信息字段
ALTER TABLE exec_task_item ADD error_message varchar(50) NULL COMMENT '异常信息';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

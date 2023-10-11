-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS functional_case(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `num` INT NOT NULL   COMMENT '业务ID' ,
    `custom_num` VARCHAR(64) NOT NULL   COMMENT '自定义业务ID' ,
    `module_id` VARCHAR(50) NOT NULL  DEFAULT '' COMMENT '模块ID' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `name` VARCHAR(255) NOT NULL   COMMENT '名称' ,
    `review_status` VARCHAR(64) NOT NULL  DEFAULT 'PREPARE' COMMENT '评审状态：未开始/进行中/已完成/已结束' ,
    `tags` VARCHAR(1000)    COMMENT '标签（JSON)' ,
    `case_model` VARCHAR(50) NOT NULL  DEFAULT 'STEP' COMMENT '编辑模式：步骤模式/文本模式' ,
    `pos` BIGINT NOT NULL  DEFAULT 0 COMMENT '自定义排序，间隔5000' ,
    `version_id` VARCHAR(50) NOT NULL   COMMENT '版本ID' ,
    `ref_id` VARCHAR(50) NOT NULL   COMMENT '指向初始版本ID' ,
    `last_execute_result` VARCHAR(64) NOT NULL  DEFAULT 'PREPARE' COMMENT '最近的执行结果：未执行/通过/失败/阻塞/跳过' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否在回收站：0-否，1-是' ,
    `public_case` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是公共用例：0-否，1-是' ,
    `latest` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否为最新版本：0-否，1-是' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `update_user` VARCHAR(50)    COMMENT '更新人' ,
    `delete_user` VARCHAR(50)    COMMENT '删除人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `delete_time` BIGINT    COMMENT '删除时间' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例';


CREATE INDEX idx_module_id ON functional_case(module_id);
CREATE INDEX idx_project_id_pos ON functional_case(project_id,pos);
CREATE INDEX idx_public_case_pos ON functional_case(public_case,pos);
CREATE INDEX idx_ref_id ON functional_case(ref_id);
CREATE INDEX idx_version_id ON functional_case(version_id);


CREATE TABLE IF NOT EXISTS functional_case_blob(
    `id` VARCHAR(50) NOT NULL   COMMENT '功能用例ID' ,
    `steps` LONGTEXT    COMMENT '用例步骤（JSON)，step_model 为 Step 时启用' ,
    `text_description` LONGTEXT    COMMENT '步骤描述，step_model 为 Text 时启用' ,
    `expected_result` LONGTEXT    COMMENT '预期结果，step_model 为 Text  时启用' ,
    `prerequisite` LONGTEXT    COMMENT '前置条件' ,
    `description` LONGTEXT    COMMENT '备注' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例';


CREATE TABLE IF NOT EXISTS functional_case_comment(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `case_id` VARCHAR(50) NOT NULL   COMMENT '功能用例ID' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '评论人' ,
    `status` VARCHAR(64)    COMMENT '评审/测试计划执行状态:通过/不通过/重新提审/通过标准变更标记/强制通过标记/强制不通过标记/状态变更标记' ,
    `type` VARCHAR(64) NOT NULL  DEFAULT 'CASE' COMMENT '评论类型：用例评论/测试计划用例评论/评审用例评论' ,
    `belong_id` VARCHAR(50)    COMMENT '评审ID' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `description` TEXT NOT NULL   COMMENT '内容' ,
    `reply_user` VARCHAR(50)    COMMENT '回复人' ,
    `parent_id` VARCHAR(50)   DEFAULT '' COMMENT '父评论id' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例评论';


CREATE INDEX idx_create_time ON functional_case_comment(create_time);
CREATE INDEX idx_case_id ON functional_case_comment(case_id);
CREATE INDEX idx_status ON functional_case_comment(status);
CREATE INDEX idx_type ON functional_case_comment(type);
CREATE INDEX idx_belong_id ON functional_case_comment(belong_id);


CREATE TABLE IF NOT EXISTS functional_case_module(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `name` VARCHAR(100) NOT NULL   COMMENT '名称' ,
    `parent_id` VARCHAR(50) NOT NULL   COMMENT '父节点ID' ,
    `level` INT NOT NULL  DEFAULT 1 COMMENT '节点的层级' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `pos` BIGINT(10) NOT NULL  DEFAULT 0 COMMENT '同一节点下的顺序' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例模块';


CREATE INDEX idx_project_id ON functional_case_module(project_id);
CREATE INDEX idx_name ON functional_case_module(name);


CREATE TABLE IF NOT EXISTS functional_case_attachment(
    `case_id` VARCHAR(50) NOT NULL   COMMENT '功能用例ID' ,
    `file_id` VARCHAR(50) NOT NULL   COMMENT '文件的ID' ,
    PRIMARY KEY (case_id,file_id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例和附件的中间表';


CREATE TABLE IF NOT EXISTS functional_case_follower(
    `case_id` VARCHAR(50) NOT NULL   COMMENT '功能用例ID' ,
    `user_id` VARCHAR(50) NOT NULL   COMMENT '关注人ID' ,
    PRIMARY KEY (case_id,user_id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例和关注人的中间表';


CREATE TABLE IF NOT EXISTS functional_case_relationship_edge(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `source_id` VARCHAR(50) NOT NULL   COMMENT '源节点的ID' ,
    `target_id` VARCHAR(50) NOT NULL   COMMENT '目标节点的ID' ,
    `graph_id` VARCHAR(50) NOT NULL   COMMENT '所属关系图的ID' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例的前后置关系';


CREATE INDEX source_id_index ON functional_case_relationship_edge(source_id);
CREATE INDEX target_id_index ON functional_case_relationship_edge(target_id);


CREATE TABLE IF NOT EXISTS functional_case_test(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `case_id` VARCHAR(50) NOT NULL   COMMENT '功能用例ID' ,
    `test_id` VARCHAR(50) NOT NULL   COMMENT '其他类型用例ID' ,
    `test_type` VARCHAR(64) NOT NULL   COMMENT '用例类型：接口用例/场景用例/性能用例/UI用例' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例和其他用例的中间表';


CREATE UNIQUE INDEX uk_functional_case_id_test_id ON functional_case_test(case_id,test_id);
CREATE INDEX idx_test_id ON functional_case_test(test_id);


CREATE TABLE IF NOT EXISTS functional_minder_extra_node(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `parent_id` VARCHAR(50) NOT NULL   COMMENT '父节点的ID，即模块ID' ,
    `group_id` VARCHAR(50) NOT NULL   COMMENT '项目ID，可扩展为其他资源ID' ,
    `node_data` LONGTEXT NOT NULL   COMMENT '存储脑图节点额外信息' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例脑图临时节点';


CREATE INDEX idx_parent_id ON functional_minder_extra_node(parent_id);


CREATE TABLE IF NOT EXISTS functional_case_custom_field(
    `case_id` VARCHAR(50) NOT NULL   COMMENT '资源ID' ,
    `field_id` VARCHAR(50) NOT NULL   COMMENT '字段ID' ,
    `value` VARCHAR(1000)    COMMENT '字段值' ,
    `text_value` LONGTEXT    COMMENT '富文本类型字段值' ,
    PRIMARY KEY (case_id,field_id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '自定义字段功能用例关系';


CREATE TABLE IF NOT EXISTS case_review(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `name` VARCHAR(200) NOT NULL   COMMENT '名称' ,
    `status` VARCHAR(64) NOT NULL  DEFAULT 'PREPARE' COMMENT '评审状态：未开始/进行中/已完成/已结束/已归档' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `end_time` BIGINT NOT NULL   COMMENT '评审结束时间' ,
    `description` TEXT    COMMENT '描述' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `tags` VARCHAR(1000)    COMMENT '标签' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `review_pass_rule` VARCHAR(64) NOT NULL  DEFAULT 'SINGLE' COMMENT '评审规则：单人通过/全部通过' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '用例评审';


CREATE INDEX idx_create_user ON case_review(create_user);
CREATE INDEX idx_project_id ON case_review(project_id);
CREATE INDEX idx_name ON case_review(name);
CREATE INDEX idx_status ON case_review(status);
CREATE INDEX idx_review_pass_rule ON case_review(review_pass_rule);
CREATE INDEX idx_create_time ON case_review(create_time);
CREATE INDEX idx_end_time ON case_review(end_time);
CREATE INDEX idx_update_time ON case_review(update_time);


CREATE TABLE IF NOT EXISTS case_review_user(
    `review_id` VARCHAR(50) NOT NULL   COMMENT '评审ID' ,
    `user_id` VARCHAR(50) NOT NULL   COMMENT '评审人ID' ,
    PRIMARY KEY (review_id,user_id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '评审和评审人中间表';


CREATE TABLE IF NOT EXISTS case_review_functional_case(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `review_id` VARCHAR(50) NOT NULL   COMMENT '评审ID' ,
    `case_id` VARCHAR(50) NOT NULL   COMMENT '用例ID' ,
    `status` VARCHAR(64) NOT NULL  DEFAULT 'UNDERWAY' COMMENT '评审状态：进行中/通过/不通过/重新提审' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `pos` BIGINT NOT NULL  DEFAULT 0 COMMENT '自定义排序，间隔5000' ,
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '关联的用例是否放入回收站' ,
    PRIMARY KEY (id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '用例评审和功能用例的中间表';


CREATE INDEX idx_case_id ON case_review_functional_case(case_id);
CREATE INDEX idx_review_id ON case_review_functional_case(review_id);


CREATE TABLE IF NOT EXISTS case_review_functional_case_user(
    `case_id` VARCHAR(50) NOT NULL   COMMENT '功能用例和评审中间表的ID' ,
    `review_id` VARCHAR(50) NOT NULL   COMMENT '评审ID' ,
    `user_id` VARCHAR(50) NOT NULL   COMMENT '评审人ID'
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '功能用例评审和评审人的中间表';


CREATE UNIQUE INDEX test_case_review_test_case_users_pk ON case_review_functional_case_user(review_id,case_id,user_id);


CREATE TABLE IF NOT EXISTS case_review_follower(
    `review_id` VARCHAR(50) NOT NULL   COMMENT '评审ID' ,
    `user_id` VARCHAR(50) NOT NULL   COMMENT '关注人' ,
    PRIMARY KEY (review_id,user_id)
    )  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = '用例评审关注人';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS functional_case
(
    `id`                  VARCHAR(50)  NOT NULL COMMENT 'ID',
    `num`                 BIGINT       NOT NULL COMMENT '业务ID',
    `module_id`           VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '模块ID',
    `project_id`          VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `template_id`         VARCHAR(50)  NOT NULL COMMENT '模板ID',
    `name`                VARCHAR(255) NOT NULL COMMENT '名称',
    `review_status`       VARCHAR(64)  NOT NULL DEFAULT 'UN_REVIEWED' COMMENT '评审结果：未评审/评审中/通过/不通过/重新提审',
    `tags`                VARCHAR(1000) COMMENT '标签（JSON)',
    `case_edit_type`      VARCHAR(50)  NOT NULL DEFAULT 'STEP' COMMENT '编辑模式：步骤模式/文本模式',
    `pos`                 BIGINT       NOT NULL DEFAULT 0 COMMENT '自定义排序，间隔5000',
    `version_id`          VARCHAR(50)  NOT NULL COMMENT '版本ID',
    `ref_id`              VARCHAR(50)  NOT NULL COMMENT '指向初始版本ID',
    `last_execute_result` VARCHAR(64)  NOT NULL DEFAULT 'UN_EXECUTED' COMMENT '最近的执行结果：未执行/通过/失败/阻塞/跳过',
    `deleted`             BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否在回收站：0-否，1-是',
    `public_case`         BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否是公共用例：0-否，1-是',
    `latest`              BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否为最新版本：0-否，1-是',
    `create_user`         VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user`         VARCHAR(50) COMMENT '更新人',
    `delete_user`         VARCHAR(50) COMMENT '删除人',
    `create_time`         BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`         BIGINT       NOT NULL COMMENT '更新时间',
    `delete_time`         BIGINT COMMENT '删除时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例';

CREATE INDEX idx_module_id ON functional_case (module_id);
CREATE INDEX idx_project_id_pos ON functional_case (project_id, pos);
CREATE INDEX idx_public_case_pos ON functional_case (public_case, pos);
CREATE INDEX idx_ref_id ON functional_case (ref_id);
CREATE INDEX idx_version_id ON functional_case (version_id);
CREATE INDEX idx_create_time ON functional_case (create_time desc);
CREATE INDEX idx_delete_time ON functional_case (delete_time desc);
CREATE INDEX idx_update_time ON functional_case (update_time desc);
CREATE INDEX idx_num ON functional_case (num);
CREATE INDEX idx_project_id ON functional_case (project_id);
CREATE INDEX idx_pos ON functional_case (pos);


CREATE TABLE IF NOT EXISTS functional_case_blob
(
    `id`               VARCHAR(50) NOT NULL COMMENT '功能用例ID',
    `steps`            LONGBLOB COMMENT '用例步骤（JSON)，step_model 为 Step 时启用',
    `text_description` LONGBLOB COMMENT '文本描述，step_model 为 Text 时启用',
    `expected_result`  LONGBLOB COMMENT '预期结果，step_model 为 Text  时启用',
    `prerequisite`     LONGBLOB COMMENT '前置条件',
    `description`      LONGBLOB COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例';


CREATE TABLE IF NOT EXISTS functional_case_comment
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `case_id`     VARCHAR(50) NOT NULL COMMENT '功能用例ID',
    `create_user` VARCHAR(50) NOT NULL COMMENT '评论人',
    `parent_id`   VARCHAR(50) COMMENT '父评论ID',
    `resource_id` VARCHAR(50) COMMENT '资源ID: 评审ID/测试计划ID',
    `notifier`    VARCHAR(1000) COMMENT '通知人',
    `content`     TEXT        NOT NULL COMMENT '内容',
    `reply_user`  VARCHAR(50) COMMENT '回复人',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例评论';


CREATE INDEX idx_create_time ON functional_case_comment (create_time desc);
CREATE INDEX idx_case_id ON functional_case_comment (case_id);
CREATE INDEX idx_resource_id ON functional_case_comment (resource_id);

CREATE TABLE IF NOT EXISTS functional_case_module
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `parent_id`   VARCHAR(50)  NOT NULL DEFAULT 'NONE' COMMENT '父节点ID',
    `pos`         BIGINT       NOT NULL DEFAULT 0 COMMENT '同一节点下的顺序',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user` VARCHAR(50)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例模块';


CREATE INDEX idx_project_id ON functional_case_module (project_id);
CREATE INDEX idx_name ON functional_case_module (name);
CREATE INDEX idx_pos ON functional_case_module (pos);
CREATE INDEX idx_parent_id ON functional_case_module (parent_id);
CREATE INDEX idx_create_user ON functional_case_module (create_user);
CREATE INDEX idx_update_user ON functional_case_module (update_user);
CREATE INDEX idx_create_time ON functional_case_module (create_time desc);
CREATE INDEX idx_update_time ON functional_case_module (update_time desc);
CREATE UNIQUE INDEX uq_name_project_parent ON functional_case_module(project_id,name,parent_id);

CREATE TABLE IF NOT EXISTS functional_case_attachment
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'id',
    `case_id`     VARCHAR(50)  NOT NULL COMMENT '功能用例ID',
    `file_id`     VARCHAR(50)  NOT NULL COMMENT '文件的ID',
    `file_name`   VARCHAR(255) NOT NULL COMMENT '文件名称',
    `file_source` VARCHAR(50) NOT NULL  DEFAULT 'ATTACHMENT' COMMENT '文件来源' ,
    `size`        BIGINT       NOT NULL COMMENT '文件大小',
    `local`       BIT(1)       NOT NULL COMMENT '是否本地',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例和附件的中间表';


CREATE INDEX idx_case_id ON functional_case_attachment (case_id);
CREATE INDEX idx_local ON functional_case_attachment (local);
CREATE INDEX idx_file_id ON functional_case_attachment (file_id);
CREATE INDEX idx_file_name ON functional_case_attachment (file_name);
CREATE INDEX idx_file_source ON functional_case_attachment(file_source);


CREATE TABLE IF NOT EXISTS functional_case_follower
(
    `case_id` VARCHAR(50) NOT NULL COMMENT '功能用例ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '关注人ID',
    PRIMARY KEY (case_id, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例和关注人的中间表';


CREATE TABLE IF NOT EXISTS functional_case_relationship_edge
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `source_id`   VARCHAR(50) NOT NULL COMMENT '源节点的ID',
    `target_id`   VARCHAR(50) NOT NULL COMMENT '目标节点的ID',
    `graph_id`    VARCHAR(50) NOT NULL COMMENT '所属关系图的ID',
    `create_user` VARCHAR(50) NOT NULL COMMENT '创建人',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例的前后置关系';


CREATE INDEX source_id_index ON functional_case_relationship_edge (source_id);
CREATE INDEX target_id_index ON functional_case_relationship_edge (target_id);


CREATE TABLE IF NOT EXISTS functional_case_test
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `case_id`     VARCHAR(50) NOT NULL COMMENT '功能用例ID',
    `source_id`   VARCHAR(50) NOT NULL COMMENT '其他类型用例ID',
    `source_type` VARCHAR(64) NOT NULL COMMENT '用例类型：接口用例/场景用例/性能用例/UI用例',
    `project_id` VARCHAR(50) NOT NULL   COMMENT '用例所属项目' ,
    `version_id` VARCHAR(50) NOT NULL   COMMENT '用例的版本id' ,
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50) NOT NULL COMMENT '创建人',
    `update_user` VARCHAR(50) NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例和其他用例的中间表';


CREATE INDEX idx_case_id ON functional_case_test (case_id);
CREATE INDEX idx_source_id ON functional_case_test (source_id);
CREATE INDEX idx_source_type ON functional_case_test(source_type);
CREATE INDEX idx_project_id ON functional_case_test(project_id);

CREATE TABLE IF NOT EXISTS functional_case_demand
(
    `id`              VARCHAR(50) NOT NULL COMMENT 'ID',
    `case_id`         VARCHAR(50) NOT NULL COMMENT '功能用例ID',
    `parent`          VARCHAR(255) NOT NULL DEFAULT 'NONE' COMMENT '父需求id',
    `with_parent`     BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否与父节点一起关联：0-否，1-是' ,
    `demand_id`       VARCHAR(255) COMMENT '需求ID',
    `demand_name`     VARCHAR(255) NOT NULL DEFAULT 'NONE' COMMENT '需求标题',
    `demand_url`      VARCHAR(255) COMMENT '需求地址',
    `demand_platform` VARCHAR(64) NOT NULL DEFAULT 'LOCAL' COMMENT '需求所属平台',
    `create_time`     BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(50) NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(50) NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例和需求的中间表';


CREATE INDEX idx_case_id ON functional_case_demand (case_id);
CREATE INDEX idx_demand_platform ON functional_case_demand (demand_platform);

CREATE TABLE IF NOT EXISTS functional_minder_extra_node
(
    `id`        VARCHAR(50) NOT NULL COMMENT 'ID',
    `parent_id` VARCHAR(50) NOT NULL COMMENT '父节点的ID，即模块ID',
    `group_id`  VARCHAR(50) NOT NULL COMMENT '项目ID，可扩展为其他资源ID',
    `node_data` LONGTEXT    NOT NULL COMMENT '存储脑图节点额外信息',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例脑图临时节点';


CREATE INDEX idx_parent_id ON functional_minder_extra_node (parent_id);


CREATE TABLE IF NOT EXISTS functional_case_custom_field
(
    `case_id`  VARCHAR(50) NOT NULL COMMENT '资源ID',
    `field_id` VARCHAR(50) NOT NULL COMMENT '字段ID',
    `value`    VARCHAR(1000) COMMENT '字段值',
    PRIMARY KEY (case_id, field_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '自定义字段功能用例关系';


CREATE TABLE IF NOT EXISTS case_review
(
    `id`               VARCHAR(50)   NOT NULL COMMENT 'ID',
    `num`              BIGINT        NOT NULL COMMENT '业务ID',
    `name`             VARCHAR(255)  NOT NULL COMMENT '名称',
    `module_id`        VARCHAR(50)   NOT NULL COMMENT '模块id',
    `project_id`       VARCHAR(50)   NOT NULL COMMENT '项目ID',
    `status`           VARCHAR(64)   NOT NULL DEFAULT 'PREPARE' COMMENT '评审状态：未开始/进行中/已完成/已结束/已归档',
    `review_pass_rule` VARCHAR(64)   NOT NULL DEFAULT 'SINGLE' COMMENT '通过标准：单人通过/全部通过',
    `pos`              BIGINT        NOT NULL DEFAULT 0 COMMENT '自定义排序，间隔5000',
    `start_time`       BIGINT COMMENT '评审开始时间',
    `end_time`         BIGINT COMMENT '评审结束时间',
    `case_count`       INT           NOT NULL DEFAULT 0 COMMENT '用例数',
    `pass_rate`        DECIMAL(5, 2) NOT NULL DEFAULT 0.00 COMMENT '通过率(保留两位小数)',
    `tags`             VARCHAR(1000) COMMENT '标签',
    `description`      VARCHAR(1000) COMMENT '描述',
    `create_time`      BIGINT        NOT NULL COMMENT '创建时间',
    `create_user`      VARCHAR(50)   NOT NULL COMMENT '创建人',
    `update_time`      BIGINT        NOT NULL COMMENT '更新时间',
    `update_user`      VARCHAR(50)   NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用例评审';


CREATE INDEX idx_create_user ON case_review (create_user);
CREATE INDEX idx_project_id ON case_review (project_id);
CREATE INDEX idx_name ON case_review (name);
CREATE INDEX idx_status ON case_review (status);
CREATE INDEX idx_review_pass_rule ON case_review (review_pass_rule);
CREATE INDEX idx_create_time ON case_review (create_time desc);
CREATE INDEX idx_update_time ON case_review (update_time desc);
CREATE INDEX idx_update_user ON case_review (update_user);
CREATE INDEX idx_module_id ON case_review (module_id);
CREATE INDEX idx_pos ON case_review (pos);
CREATE INDEX idx_case_count ON case_review (case_count);
CREATE INDEX idx_pass_rate ON case_review (pass_rate);
CREATE INDEX idx_num ON case_review (num);


CREATE TABLE IF NOT EXISTS case_review_user
(
    `review_id` VARCHAR(50) NOT NULL COMMENT '评审ID',
    `user_id`   VARCHAR(50) NOT NULL COMMENT '评审人ID',
    PRIMARY KEY (review_id, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '评审和评审人中间表';


CREATE TABLE IF NOT EXISTS case_review_functional_case
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `review_id`   VARCHAR(50) NOT NULL COMMENT '评审ID',
    `case_id`     VARCHAR(50) NOT NULL COMMENT '用例ID',
    `status`      VARCHAR(64) NOT NULL DEFAULT 'UNDERWAY' COMMENT '评审状态：进行中/通过/不通过/重新提审',
    `create_time` BIGINT      NOT NULL COMMENT '创建时间',
    `create_user` VARCHAR(50) NOT NULL COMMENT '创建人',
    `update_time` BIGINT      NOT NULL COMMENT '更新时间',
    `pos`         BIGINT      NOT NULL DEFAULT 0 COMMENT '自定义排序，间隔5000',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用例评审和功能用例的中间表';


CREATE INDEX idx_case_id ON case_review_functional_case (case_id);
CREATE INDEX idx_review_id ON case_review_functional_case (review_id);
CREATE INDEX idx_status ON case_review_functional_case (status);
CREATE INDEX idx_pos ON case_review_functional_case (pos);
CREATE UNIQUE INDEX idx_case_id_review_id ON case_review_functional_case(review_id,case_id);



CREATE TABLE IF NOT EXISTS case_review_functional_case_archive
(
    `review_id` VARCHAR(50) NOT NULL COMMENT '用例评审ID',
    `case_id`   VARCHAR(50) NOT NULL COMMENT '功能用例ID',
    `content`   LONGBLOB COMMENT '功能用例快照（JSON)'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用例评审归档表';


CREATE INDEX idx_review_id ON case_review_functional_case_archive (review_id);
CREATE INDEX idx_case_id ON case_review_functional_case_archive (case_id);
CREATE INDEX idx_review_id_case_id ON case_review_functional_case_archive (review_id, case_id);


CREATE TABLE IF NOT EXISTS case_review_functional_case_user
(
    `case_id`   VARCHAR(50) NOT NULL COMMENT '功能用例ID',
    `review_id` VARCHAR(50) NOT NULL COMMENT '评审ID',
    `user_id`   VARCHAR(50) NOT NULL COMMENT '评审人ID'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '功能用例评审和评审人的中间表';


CREATE UNIQUE INDEX idx_case_review_user ON case_review_functional_case_user (review_id, user_id, case_id);
CREATE INDEX idx_case_id_review_id ON case_review_functional_case_user (case_id, review_id);
CREATE INDEX idx_case_id ON case_review_functional_case_user (case_id);
CREATE INDEX idx_review_id ON case_review_functional_case_user (review_id);
CREATE INDEX idx_user_id ON case_review_functional_case_user (user_id);


CREATE TABLE IF NOT EXISTS case_review_follower
(
    `review_id` VARCHAR(50) NOT NULL COMMENT '评审ID',
    `user_id`   VARCHAR(50) NOT NULL COMMENT '关注人',
    PRIMARY KEY (review_id, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用例评审和关注人的中间表';

CREATE TABLE IF NOT EXISTS case_review_history
(
    `id`          VARCHAR(50) NOT NULL COMMENT 'ID',
    `review_id`   VARCHAR(50) NOT NULL COMMENT '评审ID',
    `case_id`     VARCHAR(50) NOT NULL COMMENT '用例ID',
    `content`     LONGBLOB COMMENT '评审意见',
    `status`      VARCHAR(64) NOT NULL COMMENT '评审结果：通过/不通过/建议',
    `deleted` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是取消关联或评审被删除的：0-否，1-是' ,
    `abandoned` BIT(1) NOT NULL  DEFAULT 0 COMMENT '是否是废弃的评审记录：0-否，1-是' ,
    `notifier`    VARCHAR(1000) COMMENT '通知人',
    `create_user` VARCHAR(50) NOT NULL COMMENT '操作人',
    `create_time` BIGINT      NOT NULL COMMENT '操作时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '评审历史表';


CREATE INDEX idx_case_id ON case_review_history (case_id);
CREATE INDEX idx_review_id ON case_review_history (review_id);
CREATE INDEX idx_review_id_case_id ON case_review_history (review_id, case_id);
CREATE INDEX idx_status ON case_review_history (status);
CREATE INDEX idx_deleted ON case_review_history(deleted);
CREATE INDEX idx_abandoned ON case_review_history(abandoned);


CREATE TABLE IF NOT EXISTS case_review_module
(
    `id`          VARCHAR(50)  NOT NULL COMMENT 'ID',
    `project_id`  VARCHAR(50)  NOT NULL COMMENT '项目ID',
    `name`        VARCHAR(255) NOT NULL COMMENT '名称',
    `parent_id`   VARCHAR(50)  NOT NULL DEFAULT 'NONE' COMMENT '父节点ID',
    `pos`         BIGINT       NOT NULL DEFAULT 0 COMMENT '同一节点下的顺序',
    `create_time` BIGINT       NOT NULL COMMENT '创建时间',
    `update_time` BIGINT       NOT NULL COMMENT '更新时间',
    `create_user` VARCHAR(50)  NOT NULL COMMENT '创建人',
    `update_user` VARCHAR(50)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用例评审模块';


CREATE INDEX idx_project_id ON case_review_module (project_id);
CREATE INDEX idx_name ON case_review_module (name);
CREATE INDEX idx_pos ON case_review_module (pos);
CREATE INDEX idx_parent_id ON case_review_module (parent_id);
CREATE INDEX idx_create_user ON case_review_module (create_user);
CREATE INDEX idx_update_user ON case_review_module (update_user);
CREATE INDEX idx_create_time ON case_review_module (create_time desc);
CREATE INDEX idx_update_time ON case_review_module (update_time desc);
CREATE UNIQUE INDEX uq_name_project_parent ON case_review_module(name,project_id,parent_id);


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
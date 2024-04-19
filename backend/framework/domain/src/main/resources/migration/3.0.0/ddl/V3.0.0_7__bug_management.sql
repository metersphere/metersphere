-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS bug(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `num` INT NOT NULL   COMMENT '业务ID' ,
    `title` VARCHAR(255) NOT NULL   COMMENT '缺陷标题' ,
    `handle_users` VARCHAR(1000)    COMMENT '处理人集合;预留字段, 后续工作台统计可能需要' ,
    `handle_user` VARCHAR(50) NOT NULL   COMMENT '处理人' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '更新人' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
    `template_id` VARCHAR(50) NOT NULL   COMMENT '模板ID' ,
    `platform` VARCHAR(50) NOT NULL   COMMENT '缺陷平台' ,
    `status` VARCHAR(50) NOT NULL  DEFAULT '' COMMENT '状态' ,
    `tags` VARCHAR(1000)    COMMENT '标签' ,
    `platform_bug_id` VARCHAR(50)    COMMENT '第三方平台缺陷ID' ,
    `delete_user` VARCHAR(50) COMMENT '删除人',
    `delete_time` BIGINT COMMENT '删除时间',
    `deleted` BIT(1) NOT NULL   COMMENT '删除状态' ,
    `pos` BIGINT NOT NULL   COMMENT '自定义排序，间隔5000' ,
    PRIMARY KEY (id)
)  COMMENT = '缺陷' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE INDEX idx_num ON bug(num);
CREATE INDEX idx_title ON bug(title);
CREATE INDEX idx_assign_user ON bug(handle_user);
CREATE INDEX idx_create_user ON bug(create_user);
CREATE INDEX idx_create_time ON bug(create_time);
CREATE INDEX idx_update_user ON bug(update_user);
CREATE INDEX idx_update_time ON bug(update_time);
CREATE INDEX idx_project_id ON bug(project_id);
CREATE INDEX idx_platform ON bug(platform);
CREATE INDEX idx_status ON bug(status);
CREATE INDEX idx_deleted ON bug(deleted);
CREATE INDEX idx_pos ON bug(pos);


CREATE TABLE IF NOT EXISTS bug_content(
    `bug_id` VARCHAR(50) NOT NULL   COMMENT '缺陷ID' ,
    `description` LONGTEXT    COMMENT '缺陷描述' ,
    PRIMARY KEY (bug_id)
)  COMMENT = '缺陷内容' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS bug_follower(
    `bug_id` VARCHAR(50) NOT NULL   COMMENT '缺陷ID' ,
    `user_id` VARCHAR(50) NOT NULL   COMMENT '关注人ID' ,
    PRIMARY KEY (bug_id,user_id)
)  COMMENT = '缺陷关注人' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE INDEX idx_follow_id ON bug_follower(user_id);

CREATE TABLE IF NOT EXISTS bug_local_attachment(
    `id` VARCHAR(255) NOT NULL   COMMENT 'ID' ,
    `bug_id` VARCHAR(50) NOT NULL   COMMENT '缺陷ID' ,
    `file_id` VARCHAR(50) NOT NULL   COMMENT '文件ID' ,
    `file_name` VARCHAR(255) NOT NULL   COMMENT '文件名称' ,
    `size` BIGINT NOT NULL   COMMENT '文件大小' ,
    `source` VARCHAR(255) NOT NULL  DEFAULT 'ATTACHMENT' COMMENT '文件来源' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    PRIMARY KEY (id)
)  COMMENT = '缺陷本地附件' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_bug_id ON bug_local_attachment(bug_id);
CREATE INDEX idx_file_id ON bug_local_attachment(file_id);
CREATE INDEX idx_source ON bug_local_attachment(source);

CREATE TABLE IF NOT EXISTS bug_comment(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `bug_id` VARCHAR(50) NOT NULL   COMMENT '缺陷ID' ,
    `reply_user` VARCHAR(50)    COMMENT '回复人' ,
    `notifier` VARCHAR(1000)    COMMENT '通知人' ,
    `parent_id` VARCHAR(50)    COMMENT '父评论ID' ,
    `content` TEXT NOT NULL   COMMENT '内容' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '评论人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_user` VARCHAR(50) NOT NULL   COMMENT '更新人' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    PRIMARY KEY (id)
)  COMMENT = '缺陷评论' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE INDEX idx_bug_id ON bug_comment(bug_id);
CREATE INDEX idx_parent_id ON bug_comment(parent_id);

CREATE TABLE IF NOT EXISTS bug_custom_field(
    `bug_id` VARCHAR(50) NOT NULL   COMMENT '缺陷ID' ,
    `field_id` VARCHAR(50) NOT NULL   COMMENT '字段ID' ,
    `value` VARCHAR(1000)    COMMENT '字段值' ,
    PRIMARY KEY (bug_id, field_id)
)  COMMENT = '缺陷自定义字段' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS bug_relation_case(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `case_id` VARCHAR(50)    COMMENT '关联功能用例ID' ,
    `bug_id` VARCHAR(50) NOT NULL   COMMENT '缺陷ID' ,
    `case_type` VARCHAR(64) NOT NULL  DEFAULT 'functional' COMMENT '关联的用例类型;functional/api/ui/performance' ,
    `test_plan_id` VARCHAR(50)    COMMENT '关联测试计划ID' ,
    `test_plan_case_id` VARCHAR(50)    COMMENT '关联测试计划用例ID' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
    PRIMARY KEY (id)
)  COMMENT = '用例和缺陷的关联表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE INDEX idx_bug_id ON bug_relation_case(bug_id);
CREATE INDEX idx_plan_case_id ON bug_relation_case(test_plan_id,test_plan_case_id);
CREATE INDEX idx_case_id ON bug_relation_case(case_id);
CREATE INDEX idx_case_type ON bug_relation_case(case_type);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

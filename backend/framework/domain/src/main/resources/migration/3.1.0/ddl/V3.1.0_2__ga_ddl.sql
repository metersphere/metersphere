-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

DROP TABLE IF EXISTS functional_mind_insert_relation;

-- 报告添加默认布局字段
ALTER TABLE test_plan_report ADD `default_layout` BIT NOT NULL  DEFAULT 1 COMMENT '是否默认布局';
-- 邮箱邀请表增加字段
ALTER TABLE user_invite
    ADD COLUMN organization_id VARCHAR(50) COMMENT '组织ID',
    ADD COLUMN project_id      VARCHAR(50) COMMENT '项目ID';

CREATE TABLE IF NOT EXISTS test_plan_report_component(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `test_plan_report_id` VARCHAR(50) NOT NULL   COMMENT '测试计划报告ID' ,
    `name` VARCHAR(50) NOT NULL   COMMENT '组件名称' ,
    `label` VARCHAR(255) NOT NULL   COMMENT '组件标题' ,
    `type` VARCHAR(50) NOT NULL   COMMENT '组件分类' ,
    `value` LONGTEXT    COMMENT '组件内容' ,
    `pos` BIGINT NOT NULL   COMMENT '自定义排序，1开始整数递增' ,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试计划报告逐组件表';

ALTER TABLE test_plan_report_function_case ADD `test_plan_name` VARCHAR(255) COMMENT '测试计划名称';
ALTER TABLE test_plan_report_api_case ADD `test_plan_name` VARCHAR(255) COMMENT '测试计划名称';
ALTER TABLE test_plan_report_api_scenario ADD `test_plan_name` VARCHAR(255) COMMENT '测试计划名称';

-- 缺陷评论内容字段类型修改
ALTER TABLE bug_comment MODIFY content LONGTEXT;

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;




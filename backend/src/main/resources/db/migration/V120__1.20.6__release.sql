-- 初始化 sql
-- V120_1-20-6_test_case_modify
-- test_case 列表查询性能优化
CREATE INDEX test_case_project_id_order_IDX ON test_case(`project_id`, `order`);

-- 将用例状态默认值改为空字符，避免查询的时候加is null
ALTER TABLE test_case MODIFY COLUMN status varchar(25)  DEFAULT '';

UPDATE test_case SET status = '' WHERE status IS NULL;

-- V120_1-20-6_test_plan_test_review_refactor
-- 测试计划和评审用例添加回收站标志，不用关联用例表
ALTER TABLE test_plan_test_case ADD is_del tinyint(1) DEFAULT 0 COMMENT '关联的用例是否放入回收站';

ALTER TABLE test_case_review_test_case ADD is_del tinyint(1) DEFAULT 0 COMMENT '关联的用例是否放入回收站';

-- 初始化测试计划和评审回收站标志
UPDATE test_plan_test_case
    INNER JOIN test_case
    ON test_plan_test_case.case_id = test_case.id AND test_case.status = 'Trash'
SET test_plan_test_case.is_del = 1;

UPDATE test_case_review_test_case
    INNER JOIN test_case
    ON test_case_review_test_case.case_id = test_case.id AND test_case.status = 'Trash'
SET test_case_review_test_case.is_del = 1;

-- V120_1-20-6_log
-- 日志记录表添加索引
ALTER TABLE `operating_log`
    ADD INDEX oper_project_id (`project_id`);
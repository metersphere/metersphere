-- 新增字段-测试跟踪计划任务执行之后，可以添加其他备注信息
ALTER TABLE `test_plan_test_case`
    ADD COLUMN `execute_remark` text COMMENT '执行备注信息' AFTER `actual_result`;

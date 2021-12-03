-- 修复数据
update test_plan set repeat_case = 0 where repeat_case is null;
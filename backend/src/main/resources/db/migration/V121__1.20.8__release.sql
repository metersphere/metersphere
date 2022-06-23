-- 初始化 sql

-- V121_1-20-8_clear_test_case_node
-- 之前的版本不知道什么情况下生成了 level=0 的脏数据
-- 已无法复现，清理下脏数据
DELETE FROM test_case_node WHERE `level` = 0;


-- V121_1-20-8_modify_test_case_index
-- 由于公共用例查询SQL排序字段未使用原本的联合索引
-- 需新加一个联合索引
ALTER TABLE test_case ADD INDEX test_case_public_order_index(`case_public`, `order`);

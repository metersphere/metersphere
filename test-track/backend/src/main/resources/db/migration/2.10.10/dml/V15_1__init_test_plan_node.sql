SET SESSION innodb_lock_wait_timeout = 7200;

-- 1. 初始化所有项目的未规划计划模块
INSERT INTO test_plan_node SELECT UUID(), id, '未规划模块', NULL, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 'admin' FROM project;

-- 2. 将历史计划移入到未规划计划模块
UPDATE test_plan tcr SET node_id = (SELECT IF(COUNT(id) > 0, id, '') FROM test_plan_node WHERE project_id = tcr.project_id AND name = '未规划模块'),
                                node_path = '';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

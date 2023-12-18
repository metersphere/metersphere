SET SESSION innodb_lock_wait_timeout = 7200;

-- 1. 初始化所有项目的未规划评审模块
INSERT INTO test_case_review_node SELECT UUID(), id, '未规划模块', NULL, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 'admin' FROM project;

-- 2. 将历史评审移入到未规划评审模块
UPDATE test_case_review tcr SET node_id = (SELECT IF(COUNT(id) > 0, id, '') FROM test_case_review_node WHERE project_id = tcr.project_id AND name = '未规划模块'),
                                node_path = '';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

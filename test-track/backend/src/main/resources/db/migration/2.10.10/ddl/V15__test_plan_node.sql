SET SESSION innodb_lock_wait_timeout = 7200;

-- 测试计划表添加模块相关字段
ALTER TABLE test_plan ADD `node_id` varchar(50) NOT NULL COMMENT 'Node ID';
ALTER TABLE test_plan ADD `node_path` varchar(999) NOT NULL COMMENT 'Node Path';

-- 测试计划模块表
CREATE TABLE IF NOT EXISTS test_plan_node (
    `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Test case review node ID',
    `project_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Project ID this node belongs to',
    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Node name',
    `parent_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Parent node ID',
    `level` int NULL DEFAULT 1 COMMENT 'Node level',
    `create_time` bigint NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint NOT NULL COMMENT 'Update timestamp',
    `pos` double NULL DEFAULT NULL,
    `create_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `test_case_node_project_id_index`(`project_id`) USING BTREE,
    INDEX `test_case_node_parent_id_index`(`parent_id`) USING BTREE,
    INDEX `test_case_node_name_index`(`name`) USING BTREE,
    INDEX `test_case_node_level_index`(`level`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
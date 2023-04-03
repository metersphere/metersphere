CREATE TABLE `novice_statistics` (
                                     `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
                                     `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
                                     `guide_step` tinyint NOT NULL DEFAULT '0' COMMENT '新手引导完成的步骤',
                                     `guide_num` int NOT NULL DEFAULT '1' COMMENT '新手引导的次数',
                                     `data_option` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'data option (JSON format)',
                                     `create_time` bigint DEFAULT NULL,
                                     `update_time` bigint DEFAULT NULL,
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
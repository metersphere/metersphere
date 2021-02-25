CREATE TABLE `metersphere_dev`.`api_document_share`  (
 `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Api Document Share Info ID',
 `create_time` BIGINT ( 13 ) NOT NULL COMMENT 'Create timestamp',
 `create_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
 `update_time` BIGINT ( 13 ) NOT NULL COMMENT 'last visit timestamp',
 `share_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'single or batch',
 `share_api_id` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'APiDefinition.id (JSONArray format. Order by TreeSet)',
 PRIMARY KEY (`id`) USING BTREE,
 INDEX `share_type`(`share_type`) USING BTREE,
 INDEX `share_api_id`(`share_api_id`(125)) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
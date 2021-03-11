-- create tale user_header
CREATE TABLE IF NOT EXISTS `user_header`
(
    id      varchar(50)   not null,
    user_id varchar(50)   null,
    props   varchar(1000) null,
    type    varchar(150)  null,
    constraint user_header_pk
        primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- test_resource_pool add column
ALTER TABLE test_resource_pool ADD heap VARCHAR(200) NULL;
ALTER TABLE test_resource_pool ADD gc_algo VARCHAR(200) NULL;

-- create tale api_document_share
CREATE TABLE IF NOT EXISTS `api_document_share`  (
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

-- swagger_url_project
alter table swagger_url_project
    modify module_id varchar(120) null;

-- add_test_case
alter table test_case
    add demand_id varchar(120) null;

alter table test_case
    add demand_name varchar(999) null;
-- test_case_review add column
ALTER TABLE test_case_review ADD tags VARCHAR(2000) NULL;

-- alter test_plan_api_scenario
alter table test_plan_api_scenario change environment_id environment longtext null comment 'Relevance environment';

-- file add sort column
alter table file_metadata add sort int default 0;

-- add Original state
alter table api_definition add original_state varchar(64);
alter table api_scenario add original_state varchar(64);
update api_definition set original_state='Underway';
update api_scenario set original_state='Underway';
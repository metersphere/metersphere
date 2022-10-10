-- V128__2-1-0_update_api_scenario_load_report
--
ALTER TABLE `api_definition_exec_result`
    ADD `relevance_test_plan_report_id` varchar(50) COMMENT '关联的测试计划报告ID（可以为空)';

ALTER TABLE `api_scenario_report`
    ADD `relevance_test_plan_report_id` varchar(50) COMMENT '关联的测试计划报告ID（可以为空)';

ALTER TABLE `load_test_report`
    ADD `relevance_test_plan_report_id` varchar(50) COMMENT '关联的测试计划报告ID（可以为空)';


-- V128__2-1-0_init_permission
--
INSERT INTO user_group_permission(id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_TRACK_HOME:READ', 'PROJECT_TRACK_HOME'
FROM `group`
WHERE type = 'PROJECT';


INSERT INTO user_group_permission(id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_API_HOME:READ', 'PROJECT_API_HOME'
FROM `group`
WHERE type = 'PROJECT';


INSERT INTO user_group_permission(id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_PERFORMANCE_HOME:READ', 'PROJECT_PERFORMANCE_HOME'
FROM `group`
WHERE type = 'PROJECT';

-- V2_1_create_API_sync_rule
--
CREATE TABLE api_sync_rule_relation
(
    id                    varchar(50)          NOT NULL,
    resource_id           varchar(50)          NOT NULL COMMENT '来源id',
    resource_type         varchar(50)          NOT NULL COMMENT '来源类型',
    show_update_rule      tinyint(1) DEFAULT 0 NULL COMMENT '是否显示',
    api_sync_case_request longtext             NULL COMMENT '同步规则',
    case_creator          tinyint(1) DEFAULT 1 NULL,
    scenario_creator      tinyint(1) DEFAULT 1 NULL,
    sync_case             tinyint(1) DEFAULT 0 NULL COMMENT '是否同步用例',
    send_notice           tinyint(1) DEFAULT 0 NULL COMMENT '是否发送通知',
    PRIMARY KEY (`id`),
    INDEX `resource_id_index` (`resource_id`)
) ENGINE = INNODB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- V129__2-1-0_update_test_plan_report
--
ALTER TABLE test_plan_report
    ADD COLUMN `is_ui_scenario_executing` TINYINT(1) DEFAULT NULL COMMENT 'is Ui Scenario executing' AFTER is_scenario_executing;


-- V129__2-1-0_add_index
--
ALTER TABLE `api_definition_exec_result`
    ADD INDEX index_relevance_test_plan_report_id (`relevance_test_plan_report_id`);
ALTER TABLE `api_scenario_report`
    ADD INDEX index_relevance_test_plan_report_id (`relevance_test_plan_report_id`);
ALTER TABLE `load_test_report`
    ADD INDEX index_relevance_test_plan_report_id (`relevance_test_plan_report_id`);

-- V129__2-1-0_drop_useless_column
--
ALTER TABLE test_case DROP COLUMN other_test_name;
ALTER TABLE test_plan DROP COLUMN test_case_match_rule;
ALTER TABLE test_plan DROP COLUMN executor_match_rule;

-- V130_2-1-0_Add_Ui_Index
--
ALTER TABLE `ui_scenario_module`    ADD INDEX index_project_id (`project_id`);

ALTER TABLE `ui_scenario`    ADD INDEX index_project_id_status_module_id (`project_id`,`status`,`module_id`);

-- V128__2-1-0_feat_file_manage
-- file_metadata 新增字段
ALTER TABLE `file_metadata`
    ADD storage VARCHAR(50) DEFAULT NULL COMMENT '文件存储方式';

ALTER TABLE `file_metadata`
    ADD create_user VARCHAR(100) DEFAULT NULL COMMENT '创建人';

ALTER TABLE `file_metadata`
    ADD update_user VARCHAR(100) DEFAULT NULL COMMENT '修改人';

ALTER TABLE `file_metadata`
    ADD tags VARCHAR(2000) DEFAULT NULL COMMENT '标签';

ALTER TABLE `file_metadata`
    ADD description LONGTEXT DEFAULT NULL COMMENT '描述';

ALTER TABLE `file_metadata`
    ADD module_id VARCHAR(50) DEFAULT NULL COMMENT '文件所属模块';

ALTER TABLE `file_metadata`
    ADD load_jar TINYINT(1) DEFAULT 0 COMMENT '是否加载jar（开启后用于接口测试执行时使用）';

ALTER TABLE `file_metadata`
    ADD path VARCHAR(1000) DEFAULT NULL COMMENT '文件存储路径';

ALTER TABLE `file_metadata`
    ADD resource_type VARCHAR(50) DEFAULT NULL COMMENT '资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据';

ALTER TABLE api_execution_queue_detail
    ADD COLUMN project_ids VARCHAR(2000) DEFAULT "[]" COMMENT '项目ID集合';

-- 补充索引
ALTER TABLE file_metadata
    ADD INDEX module_id_index (`module_id`),
    ADD INDEX project_id_index (`project_id`);


-- jar_config 数据合并到 file_metadata
-- 合并项目级数据
INSERT INTO file_metadata (id,
                           NAME,
                           type,
                           create_time,
                           update_time,
                           project_id,
                           create_user,
                           update_user,
                           load_jar,
                           path,
                           resource_type,
                           description,
                           size)
SELECT id,
       NAME,
       'JAR',
       create_time,
       update_time,
       resource_id,
       creator,
       creator,
       1,
       path,
       resource_type,
       description,
       0
FROM jar_config
WHERE resource_type = 'PROJECT';


-- 合并工作空间级别数据
INSERT INTO file_metadata (id,
                           NAME,
                           type,
                           create_time,
                           update_time,
                           project_id,
                           create_user,
                           update_user,
                           load_jar,
                           path,
                           resource_type,
                           description,
                           size)
SELECT UUID(),
       j.NAME,
       'JAR',
       j.create_time,
       j.update_time,
       p.id project_id,
       j.creator,
       j.creator,
       1,
       j.path,
       j.resource_type,
       j.description,
       0
FROM jar_config j
         JOIN workspace w ON j.resource_id = w.id
         JOIN project p ON w.id = p.workspace_id;


-- 初始化权限
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ', 'PROJECT_FILE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+DOWNLOAD+JAR', 'PROJECT_FILE');


INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+BATCH+DELETE', 'PROJECT_FILE');


INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+BATCH+DOWNLOAD', 'PROJECT_FILE');


INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+BATCH+MOVE', 'PROJECT_FILE');


-- 附件引用关系表
CREATE TABLE IF NOT EXISTS `file_association`
(
    `id`               varchar(50) NOT NULL,
    `type`             varchar(50) NOT NULL COMMENT '模块类型,服务拆分后就是各个服务',
    `source_id`        varchar(50) NOT NULL COMMENT '各个模块关联时自身Id/比如API/CASE/SCENAEIO',
    `source_item_id`   varchar(50) NOT NULL COMMENT '对应资源引用时具体id，如一个用例引用多个文件',
    `file_metadata_id` varchar(50) NOT NULL COMMENT '文件id',
    `file_type`        varchar(50) NOT NULL COMMENT '文件类型',
    `project_id`       varchar(50) NOT NULL COMMENT '项目id',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `source_id` (`source_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 补充关系表索引
ALTER TABLE file_association
    ADD INDEX file_metadata_id_index (`file_metadata_id`),
    ADD INDEX project_id_index (`project_id`);


-- V128__2-1-1_feat_file_manage
--
CREATE TABLE `file_module`
(
    `id`          varchar(50) NOT NULL COMMENT 'ID',
    `project_id`  varchar(50) NOT NULL COMMENT 'Project ID this node belongs to',
    `name`        varchar(64) NOT NULL COMMENT 'Node name',
    `parent_id`   varchar(50)  DEFAULT NULL COMMENT 'Parent node ID',
    `level`       int(10)      DEFAULT '1' COMMENT 'Node level',
    `create_time` bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13)  NOT NULL COMMENT 'Update timestamp',
    `pos`         double       DEFAULT NULL,
    `create_user` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

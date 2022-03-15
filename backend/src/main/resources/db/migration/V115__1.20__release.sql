CREATE TABLE `ui_scenario_module`
(
    `id`           varchar(50) NOT NULL COMMENT 'ui scenario node ID',
    `project_id`   varchar(50) NOT NULL COMMENT 'Project ID this node belongs to',
    `name`         varchar(64) NOT NULL COMMENT 'Node name',
    `parent_id`    varchar(50)  DEFAULT NULL COMMENT 'Parent node ID',
    `level`        int(10) DEFAULT '1' COMMENT 'Node level',
    `create_time`  bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time`  bigint(13) NOT NULL COMMENT 'Update timestamp',
    `pos`          double       DEFAULT NULL,
    `create_user` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `ui_scenario`
(
    `id`                    varchar(255) NOT NULL,
    `project_id`            varchar(50)  NOT NULL COMMENT 'Project ID this test belongs to',
    `tags`                  varchar(2000) DEFAULT NULL COMMENT 'tag list',
    `user_id`               varchar(64)   DEFAULT NULL COMMENT 'User ID',
    `module_id`             varchar(64)   DEFAULT NULL COMMENT 'Module ID',
    `module_path`           varchar(1000) DEFAULT NULL,
    `name`                  varchar(255) NOT NULL COMMENT 'ui scenario name',
    `level`                 varchar(100)  DEFAULT NULL COMMENT 'ui scenario level ',
    `status`                varchar(100) NOT NULL COMMENT 'ui scenario status ',
    `principal`             varchar(100) NOT NULL COMMENT 'ui scenario principal ',
    `step_total`            int(11) DEFAULT '0' COMMENT 'Step total ',
    `schedule`              varchar(255)  DEFAULT NULL COMMENT 'Test schedule (cron list)',
    `scenario_definition`   longtext COMMENT 'Test scenario_definition json',
    `description`           longtext COMMENT 'ui scenario description',
    `create_time`           bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time`           bigint(13) NOT NULL COMMENT 'Update timestamp',
    `pass_rate`             varchar(100)  DEFAULT NULL,
    `last_result`           varchar(100)  DEFAULT NULL,
    `report_id`             varchar(50)   DEFAULT NULL,
    `num`                   int(11) DEFAULT NULL COMMENT 'ui scenario ID',
    `original_state`        varchar(64)   DEFAULT NULL,
    `custom_num`            varchar(64)   DEFAULT NULL COMMENT 'custom num',
    `create_user`           varchar(100)  DEFAULT NULL,
    `use_url`               longtext COMMENT '步骤中用到的url',
    `version`               int(10) DEFAULT '0' COMMENT '版本号',
    `delete_time`           bigint(13) DEFAULT NULL COMMENT 'Delete timestamp',
    `delete_user_id`        varchar(64)   DEFAULT NULL COMMENT 'Delete user id',
    `execute_times`         int(11) DEFAULT NULL,
    `order`                 bigint(20) NOT NULL COMMENT '自定义排序，间隔5000',
    `environment_type`      varchar(20)   DEFAULT NULL,
    `environment_json`      longtext,
    `environment_group_id`  varchar(50)   DEFAULT NULL,
    `version_id`            varchar(50)   NOT NULL,
    `ref_id`                varchar(255)  NOT NULL,
    `latest`                tinyint(1) DEFAULT '0' COMMENT '是否为最新版本 0:否，1:是',
    PRIMARY KEY (`id`),
    KEY                     `ui_scenario_ref_id_index` (`ref_id`),
    KEY                     `ui_scenario_version_id_index` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `ui_element_module`
(
    `id`           varchar(50) NOT NULL COMMENT 'Ui scenario node ID',
    `project_id`   varchar(50) NOT NULL COMMENT 'Project ID this node belongs to',
    `name`         varchar(64) NOT NULL COMMENT 'Node name',
    `parent_id`    varchar(50)  DEFAULT NULL COMMENT 'Parent node ID',
    `level`        int(10) DEFAULT '1' COMMENT 'Node level',
    `create_time`  bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time`  bigint(13) NOT NULL COMMENT 'Update timestamp',
    `pos`          double       DEFAULT NULL,
    `create_user` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `ui_element` (
  `id` varchar(50) NOT NULL COMMENT 'Ui element ID',
  `num` int(11) DEFAULT NULL COMMENT 'Ui element ID',
  `module_id` varchar(50) NOT NULL COMMENT 'Node ID this case belongs to',
  `project_id` varchar(50) NOT NULL COMMENT 'Project ID this test belongs to',
  `name` varchar(255) NOT NULL COMMENT 'Ui element name',
  `location_type` varchar(30) NOT NULL COMMENT 'Location type',
  `location` varchar(300) NOT NULL COMMENT 'Location',
  `create_user` varchar(100) DEFAULT NULL,
  `version_id` varchar(50) NOT NULL COMMENT '版本ID',
  `ref_id` varchar(50) NOT NULL COMMENT '指向初始版本ID',
  `order`  bigint(20) NOT NULL COMMENT '自定义排序，间隔5000',
  `latest` tinyint(1) DEFAULT '0' COMMENT '是否为最新版本 0:否，1:是',
  `description` varchar(1000) DEFAULT '' COMMENT '元素描述',
  `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
  `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  PRIMARY KEY (`id`),
  KEY `ui_element_order_index` (`order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- ui 自动化引用关系表
CREATE TABLE `ui_scenario_reference` (
 `id` varchar(50) NOT NULL,
 `ui_scenario_id` varchar(255) DEFAULT NULL,
 `create_time` bigint(13) DEFAULT NULL,
 `create_user_id` varchar(64) DEFAULT NULL,
 `reference_id` varchar(255) DEFAULT NULL,
 `reference_type` varchar(255) DEFAULT NULL,
 `data_type` varchar(255) DEFAULT NULL,
 PRIMARY KEY (`id`),
 KEY `ui_scenario_id_idx` (`ui_scenario_id`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

-- module management
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('metersphere.module.ui', 'ENABLE', 'text', 1);

-- 邮件默认发件人
INSERT INTO system_parameter
SELECT 'smtp.from', param_value, type, sort
FROM system_parameter
WHERE param_key = 'smtp.account';

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan' AND index_name = 'project_id_index') THEN
        ALTER TABLE `test_plan` ADD INDEX project_id_index ( `project_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change();

ALTER TABLE `test_plan` ADD COLUMN `run_mode_config` LONGTEXT COMMENT 'request (JSON format)';

--
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+DEBUG', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+EXPORT_SCENARIO', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+CREATE', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+BATCH_COPY', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+COPY', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+DELETE', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+RUN', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+IMPORT_SCENARIO', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+MOVE_BATCH', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_SCENARIO:READ+EDIT', 'PROJECT_UI_SCENARIO');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_REPORT:READ+DELETE', 'PROJECT_UI_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_REPORT:READ', 'PROJECT_UI_REPORT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_ELEMENT:READ', 'PROJECT_UI_ELEMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_ELEMENT:READ+CREATE', 'PROJECT_UI_ELEMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_ELEMENT:READ+DELETE', 'PROJECT_UI_ELEMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_ELEMENT:READ+EDIT', 'PROJECT_UI_ELEMENT');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_UI_ELEMENT:READ+COPY', 'PROJECT_UI_ELEMENT');


--
CREATE INDEX test_case_node_project_id_index
    ON test_case_node(project_id);
CREATE INDEX test_case_node_id_index
    ON test_case(node_id);

CREATE TABLE IF NOT EXISTS `test_plan_execution_queue`
(
    `id` varchar(50) NOT NULL COMMENT 'ID',
    `report_id` varchar(100) COMMENT '测试计划报告',
    `run_mode` varchar(100) COMMENT '执行模式/scenario/api/test_paln_api/test_pan_scenario',
    create_time bigint(13)  NULL COMMENT '创建时间',
    `test_plan_id` varchar(100) COMMENT 'testPlanId',
    `resource_id` varchar(100) COMMENT 'resourceId/批次id',
    `num` int   null comment 'order',
    PRIMARY KEY (`id`),
        KEY `report_id_idx` (`report_id`),
        KEY `resource_id_index` (`resource_id`),
        KEY `num_index` (`num`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- 场景步骤结果增加简要信息
ALTER TABLE api_scenario_report_result ADD `base_info` LONGTEXT NULL;


-- quota
-- 处理移除组织时遗留的脏数据
delete
from quota
where workspace_id is null
  and id != 'workspace';

alter table quota
    add member int(10) null comment '成员数量限制';

alter table quota
    add project int(10) null comment '项目数量限制';

alter table quota
    add project_id varchar(50) null comment '项目类型配额';

alter table quota
    add vum_total decimal(10,2) null comment '总vum数';

alter table quota
    add vum_used decimal(10,2) null comment '消耗的vum数';

CREATE INDEX quota_project_id_index
    ON quota(project_id);

CREATE INDEX quota_workspace_id_index
    ON quota(workspace_id);



INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_QUOTA:READ', 'WORKSPACE_QUOTA') ;

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_QUOTA:READ+EDIT', 'WORKSPACE_QUOTA');

-- 删除场景冗余字段
ALTER TABLE api_scenario DROP COLUMN use_url;

-- 优化删除报告效率
CREATE INDEX api_definition_exec_result_integrated_report_id_IDX USING BTREE ON api_definition_exec_result (integrated_report_id);

DROP PROCEDURE IF EXISTS schema_change_api;
DELIMITER //
CREATE PROCEDURE schema_change_api() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'api_definition_exec_result' AND index_name = 'create_time_index') THEN
        ALTER TABLE `api_definition_exec_result` ADD INDEX  create_time_index ( `create_time` );
    END IF;
END//
DELIMITER ;
CALL schema_change_api();

DROP PROCEDURE IF EXISTS schema_change_api_one;
DELIMITER //
CREATE PROCEDURE schema_change_api_one() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF  EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'api_definition_exec_result' AND index_name = 'projectIdIndex') THEN
        ALTER TABLE `api_definition_exec_result` DROP INDEX projectIdIndex;
    END IF;
END//
DELIMITER ;
CALL schema_change_api_one();


DROP PROCEDURE IF EXISTS schema_change_plan;
DELIMITER //
CREATE PROCEDURE schema_change_plan() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_api_scenario' AND index_name = 'api_scenario_id_index') THEN
        ALTER TABLE `test_plan_api_scenario` ADD INDEX  api_scenario_id_index ( `api_scenario_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_plan();

DROP PROCEDURE IF EXISTS schema_change_rela_one;
DELIMITER //
CREATE PROCEDURE schema_change_rela_one() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'relationship_edge' AND index_name = 'source_id_index') THEN
        ALTER TABLE `relationship_edge` ADD INDEX  source_id_index ( `source_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_rela_one();

DROP PROCEDURE IF EXISTS schema_change_rela_two;
DELIMITER //
CREATE PROCEDURE schema_change_rela_two() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'relationship_edge' AND index_name = 'target_id_index') THEN
        ALTER TABLE `relationship_edge` ADD INDEX  target_id_index ( `target_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_rela_two();

-- 场景步骤引用表增加URL字段，记录引用的api/case/自定义请求中的地址，用于计算覆盖率
ALTER TABLE api_scenario_reference_id ADD url VARCHAR(500) NULL;
ALTER TABLE api_scenario_reference_id ADD method VARCHAR(20);
ALTER TABLE `api_scenario_reference_id` ADD INDEX index_url ( `url`);
ALTER TABLE `api_scenario_reference_id` ADD INDEX index_method ( `method` );
ALTER TABLE `api_scenario` ADD INDEX index_project_id ( `project_id`);

ALTER TABLE share_info ADD lang varchar(10) NULL;

UPDATE system_parameter SET param_value = 'http://local-selenium-grid:4444' WHERE param_key = 'base.selenium.docker.url';


-- todo create v1.21
CREATE TABLE IF NOT EXISTS custom_field_test_case
(
    resource_id varchar(50) NOT NULL,
    field_id varchar(50) NOT NULL,
    value varchar(500),
    text_value text,
    PRIMARY KEY (resource_id, field_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS custom_field_issues
(
    resource_id varchar(50) NOT NULL,
    field_id varchar(50) NOT NULL,
    value varchar(500),
    text_value text,
    PRIMARY KEY (resource_id, field_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE custom_field ADD third_part TINYINT(1) DEFAULT 0 NOT NULL;
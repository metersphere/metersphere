-- 新增字段
ALTER TABLE `swagger_url_project`
    ADD COLUMN `config` longtext COMMENT '鉴权配置信息' AFTER `mode_id`;

-- 第三方平台模板
ALTER TABLE project
    ADD platform varchar(20) DEFAULT 'Local' NOT NULL COMMENT '项目使用哪个平台的模板';
ALTER TABLE project
    ADD third_part_template tinyint(1) DEFAULT 0 NULL COMMENT '是否使用第三方平台缺陷模板';

-- 处理历史数据
UPDATE issue_template
SET platform = 'Local'
WHERE platform = 'metersphere';
UPDATE project p JOIN issue_template it ON p.issue_template_id = it.id
SET p.platform = it.platform;
UPDATE custom_field
SET `type` = 'date'
WHERE `type` = 'data';

-- 公共用例库
ALTER TABLE project
    ADD case_public tinyint(1) DEFAULT NULL COMMENT '是否开启用例公共库';
ALTER TABLE project
    ADD api_quick varchar(50) DEFAULT NULL COMMENT 'api定义快捷调试按钮';

ALTER TABLE test_case
    ADD case_public tinyint(1) DEFAULT NULL COMMENT '是否是公共用例';

-- 执行结果优化
ALTER TABLE api_scenario_report
    ADD report_version int NULL;

CREATE TABLE IF NOT EXISTS `api_scenario_report_result`
(
    `id`               varchar(50)  NOT NULL COMMENT 'ID',
    `resource_id`      VARCHAR(200) DEFAULT NULL COMMENT '请求资源 id',
    `report_id`        VARCHAR(50)  DEFAULT NULL COMMENT '报告 id',
    `create_time`      bigint(13)   NULL COMMENT '创建时间',
    `status`           varchar(100) NULL COMMENT '结果状态',
    `request_time`     bigint(13)   NULL COMMENT '请求时间',
    `total_assertions` bigint(13)   NULL COMMENT '总断言数',
    `pass_assertions`  bigint(13)   NULL COMMENT '失败断言数',
    `content`          longblob COMMENT '执行结果',
    PRIMARY KEY (`id`),
    KEY `index_resource_id` (`resource_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `api_scenario_report_structure`
(
    `id`            varchar(50) NOT NULL COMMENT 'ID',
    `report_id`     VARCHAR(50) DEFAULT NULL COMMENT '请求资源 id',
    `create_time`   bigint(13)  NULL COMMENT '创建时间',
    `resource_tree` longblob    DEFAULT NULL COMMENT '资源步骤结构树',
    `console`       LONGTEXT    DEFAULT NULL COMMENT '执行日志',
    PRIMARY KEY (`id`),
    KEY `index_report_id` (`report_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_APP_MANAGER:READ+EDIT', 'PROJECT_APP_MANAGER');

CREATE TABLE `issue_comment`
(
    `id`          varchar(64) NOT NULL,
    `issue_id`    varchar(64) DEFAULT NULL,
    `description` text,
    `author`      varchar(50) DEFAULT NULL,
    `create_time` bigint(13)  DEFAULT NULL,
    `update_time` bigint(13)  DEFAULT NULL,
    `status`      varchar(80) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `api_execution_queue`
(
     `id` varchar(50) NOT NULL COMMENT 'ID',
	 `report_id` varchar(100) COMMENT '集合报告/测试计划报告',
	 `report_type` varchar(100) COMMENT '报告类型/计划报告/单独报告',
	 `run_mode` varchar(100) COMMENT '执行模式/scenario/api/test_paln_api/test_pan_scenario',
	 `pool_id` varchar(100) DEFAULT NULL  COMMENT '执行资源池',
      create_time bigint(13)  NULL COMMENT '创建时间',
	PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `api_execution_queue_detail`
(
    `id` varchar(50) NOT NULL COMMENT 'ID',
    `queue_id` varchar(100) COMMENT '队列id',
	`sort` int COMMENT '排序',
	`report_id` varchar(100) COMMENT '报告id',
	`test_id` varchar(100) COMMENT '资源id',
	`evn_map` LONGTEXT COMMENT '环境',
	`type` varchar(100) DEFAULT NULL  COMMENT '资源类型',
    create_time bigint(13)  NULL COMMENT '创建时间',
	PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

ALTER TABLE load_test
    MODIFY name VARCHAR(255) NOT NULL COMMENT 'Test name';


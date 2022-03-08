INSERT
system_parameter
VALUES
	(
		'metersphere.module.ui',
		'ENABLE',
		'text',
		1
	);

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    `version_id`            varchar(50)   DEFAULT NULL,
    `ref_id`                varchar(255)  DEFAULT NULL,
    `latest`                tinyint(1) DEFAULT '0' COMMENT '是否为最新版本 0:否，1:是',
    PRIMARY KEY (`id`),
    KEY                     `ui_scenario_ref_id_index` (`ref_id`),
    KEY                     `ui_scenario_version_id_index` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


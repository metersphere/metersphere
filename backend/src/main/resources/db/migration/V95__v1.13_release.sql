CREATE TABLE `plugin` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(300) DEFAULT NULL COMMENT 'plugin name',
  `plugin_id` varchar(300) NOT NULL COMMENT 'Plugin id',
  `script_id` varchar(300) NOT NULL COMMENT 'Ui script id',
  `clazz_name` varchar(500) NOT NULL COMMENT 'Plugin clazzName',
  `jmeter_clazz` varchar(300) NOT NULL COMMENT 'Jmeter base clazzName',
  `source_path` varchar(300) NOT NULL COMMENT 'Plugin jar path',
  `source_name` varchar(300) NOT NULL COMMENT 'Plugin jar name',
  `form_option` longtext COMMENT 'plugin form option',
  `form_script` longtext COMMENT 'plugin form script',
  `exec_entry` varchar(300) DEFAULT NULL COMMENT 'plugin init entry class',
  `create_time` bigint(13) DEFAULT NULL,
  `update_time` bigint(13) DEFAULT NULL,
  `create_user_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
)  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;


alter table test_plan_load_case
    add test_resource_pool_id varchar(50) null;

alter table test_plan_load_case
    add load_configuration longtext null;

update test_case_node set name = '未规划用例' where name = '默认模块' and `level` = 1;
update test_case set node_path = replace (`node_path`,'/默认模块','/未规划用例') where node_path like '/默认模块%';

update api_module set name = '未规划接口' where name = '默认模块' and `level` = 1;
update api_definition set module_path = replace (`module_path`,'/默认模块','/未规划接口') where module_path like '/默认模块%';

update api_scenario_module set name = '未规划场景' where name = '默认模块' and `level` = 1;
update api_scenario set module_path = replace (`module_path`,'/默认模块','/未规划场景') where module_path like '/默认模块%';


ALTER TABLE test_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE api_test_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE api_scenario ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE load_test ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE api_definition ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE test_plan_test_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE test_plan_api_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE test_plan_api_scenario ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE test_plan_load_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';

create table if not exists custom_function
(
    id varchar(50) not null
        primary key,
    name varchar(255) null comment '函数名',
    tags varchar(1000) null comment '标签',
    description varchar(1000) null comment '函数描述',
    type varchar(255) null comment '脚本语言类型',
    params longtext null comment '参数列表',
    script longtext null comment '函数体',
    result longtext null comment '执行结果',
    create_user varchar(100) null comment '创建人',
    create_time bigint(13) null comment '创建时间',
    update_time bigint(13) null comment '更新时间',
    project_id varchar(50) null comment '所属项目ID'
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;


CREATE table if not exists `minder_extra_node` (
  `id` varchar(50) NOT NULL,
  `parent_id` varchar(50) NOT NULL COMMENT '父节点的id',
  `group_id` varchar(50) NOT NULL COMMENT '所属的项目',
  `type` varchar(30) NOT NULL COMMENT '类型，如：用例编辑脑图',
  `node_data` longtext COMMENT '存储脑图节点额外信息',
  PRIMARY KEY (`id`)
)  ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `report_statistics`
(
    `id`              varchar(50)   NOT NULL,
    `name`            varchar(50)   DEFAULT NULL,
    `project_id` 			varchar(50) 	DEFAULT NULL COMMENT 'Test plan ID',
    `create_user` 		varchar(64)   DEFAULT NULL COMMENT 'create user',
    `update_user` 		varchar(64)   DEFAULT NULL COMMENT 'create user',
    `select_option` 	longtext  		COMMENT 'select option (JSON format)',
    `data_option` 		longtext 			COMMENT 'data option (JSON format)',
    `report_type`     varchar(50)   NOT NULL,
    `create_time`     bigint(13)    DEFAULT NULL,
    `update_time`     bigint(13)    DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

create index load_test_report_detail_report_id_index
    on load_test_report_detail (report_id);


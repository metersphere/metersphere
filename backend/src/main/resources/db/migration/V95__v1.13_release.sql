CREATE TABLE `plugin` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `name` varchar(300) DEFAULT NULL COMMENT 'plugin name',
  `plugin_id` varchar(300) NOT NULL COMMENT 'Plugin id',
  `script_id` varchar(300) NOT NULL COMMENT 'Ui script id',
  `clazz_name` varchar(500) NOT NULL COMMENT 'Plugin clazzName',
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

update test_case_node set name = '未规划用例' where name = '默认模块' and `level` = 1;
update test_case set node_path = replace (`node_path`,'/默认模块','/未规划用例') where node_path like '/默认模块%';

update api_module set name = '未规划接口' where name = '默认模块' and `level` = 1;
update api_definition set module_path = replace (`module_path`,'/默认模块','/未规划接口') where module_path like '/默认模块%';

update api_scenario_module set name = '未规划场景' where name = '默认模块' and `level` = 1;
update api_scenario set module_path = replace (`module_path`,'/默认模块','/未规划场景') where module_path like '/默认模块%';


ALTER TABLE test_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';
ALTER TABLE api_test_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';

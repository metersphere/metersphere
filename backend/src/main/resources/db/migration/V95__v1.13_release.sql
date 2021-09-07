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
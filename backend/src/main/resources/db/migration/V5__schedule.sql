CREATE TABLE IF NOT EXISTS `schedule` (
  `id` varchar(50) NOT NULL COMMENT 'Schedule ID',
  `key` varchar(50) NOT NULL COMMENT 'Schedule Key',
  `type` varchar(50) NOT NULL COMMENT 'Schedule Type',
  `value` varchar(255) NOT NULL COMMENT 'Schedule value',
  `group` varchar(50) DEFAULT NULL COMMENT 'Group Name',
   `job` varchar(64) NOT NULL COMMENT 'Schedule Job Class Name',
  `enable` tinyint(1) COMMENT 'Schedule Eable',
  `resource_id` varchar(64) NOT NULL COMMENT 'Resource Id',
  `user_id` varchar(50) NOT NULL COMMENT 'Change User',
  `custom_data` longtext COMMENT 'Custom Data (JSON format)',
  PRIMARY KEY (`id`),
  KEY `resource_id` ( `resource_id` )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

ALTER TABLE `api_test` DROP COLUMN `schedule`;
ALTER TABLE `load_test` DROP COLUMN `schedule`;
ALTER TABLE `api_test_report` ADD `trigger_mode` varchar(64) NULL;
ALTER TABLE `load_test_report` ADD `trigger_mode` varchar(64) NULL;
UPDATE `api_test_report`  SET `trigger_mode` = 'MANUAL' WHERE 1;
UPDATE `load_test_report`  SET `trigger_mode` = 'MANUAL' WHERE 1;
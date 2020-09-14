CREATE TABLE `license` (
  `id` varchar(50) NOT NULL COMMENT 'ID',
  `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
  `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
  `corporation` varchar(500) NOT NULL COMMENT 'corporation ',
  `expired` varchar(255) NOT NULL COMMENT 'expired ',
  `product` varchar(500) DEFAULT NULL COMMENT 'product name',
  `edition` varchar(255) COMMENT 'edition ',
  `license_version` varchar(255) NOT NULL COMMENT 'licenseVersion',
  `license_count` INT COMMENT 'license_count',
  `license_code` longtext DEFAULT NULL COMMENT 'license_code',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
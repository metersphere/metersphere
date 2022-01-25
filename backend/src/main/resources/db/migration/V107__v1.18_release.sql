ALTER TABLE `operating_log` ADD INDEX oper_time_index ( `oper_time` );

CREATE TABLE `operating_log_resource`
(
    `id` varchar(50) NOT NULL COMMENT 'ID',
    `operating_log_id`   varchar(50) NOT NULL COMMENT 'Operating log ID',
    `source_id`    varchar(50) NOT NULL COMMENT 'operating source id',
    PRIMARY KEY (`id`),
 KEY `operating_log_id_index` (`operating_log_id`),
 KEY `source_id_index` (`source_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;
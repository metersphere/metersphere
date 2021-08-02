
-- 接口定义增加全局运行环境选择
CREATE TABLE IF NOT EXISTS `api_definition_env` (
    `id`                varchar(50) NOT NULL COMMENT 'ID',
    `user_id`            varchar(50) NOT NULL COMMENT 'user id',
    `env_id`            varchar(50) NULL COMMENT 'Api Environment id',
     create_time         bigint(13)  null,
     update_time         bigint(13)  null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS model_source(
                             `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                             `name` VARCHAR(255) NOT NULL   COMMENT '模型名称' ,
                             `type` VARCHAR(50) NOT NULL   COMMENT '模型类别（大语言/视觉/音频）' ,
                             `provider_name` VARCHAR(255) NOT NULL   COMMENT '模型供应商' ,
                             `avatar` VARCHAR(255) NOT NULL   COMMENT '模型图片' ,
                             `permission_type` VARCHAR(50) NOT NULL   COMMENT '模型类型（公有/私有）' ,
                             `status` BIT NOT NULL  DEFAULT 0 COMMENT '模型连接状态' ,
                             `owner` VARCHAR(255) NOT NULL   COMMENT '模型拥有者' ,
                             `owner_type` VARCHAR(255) NOT NULL   COMMENT '模型拥有者类型（个人/企业）' ,
                             `base_name` VARCHAR(255) NOT NULL   COMMENT '基础名称' ,
                             `app_key` VARCHAR(255) NOT NULL   COMMENT '模型key' ,
                             `api_url` VARCHAR(255) NOT NULL   COMMENT '模型url' ,
                             `adv_settings` VARCHAR(255) NOT NULL   COMMENT '模型参数配置值' ,
                             PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '模型对接保存参数';

CREATE INDEX idx_type ON model_source(`type`);


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

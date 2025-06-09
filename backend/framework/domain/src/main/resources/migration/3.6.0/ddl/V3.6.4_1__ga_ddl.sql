-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS ai_model_source(
                             `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                             `name` VARCHAR(255) NOT NULL   COMMENT '模型名称' ,
                             `type` VARCHAR(50) NOT NULL   COMMENT '模型类别（大语言/视觉/音频）' ,
                             `provider_name` VARCHAR(255) NOT NULL   COMMENT '模型供应商' ,
                             `permission_type` VARCHAR(50) NOT NULL   COMMENT '模型类型（公有/私有）' ,
                             `status` BIT NOT NULL  DEFAULT 0 COMMENT '模型连接状态' ,
                             `owner` VARCHAR(255) NOT NULL   COMMENT '模型拥有者' ,
                             `owner_type` VARCHAR(255) NOT NULL   COMMENT '模型拥有者类型（个人/企业）' ,
                             `base_name` VARCHAR(255) NOT NULL   COMMENT '基础名称' ,
                             `app_key` VARCHAR(255) NOT NULL   COMMENT '模型key' ,
                             `api_url` VARCHAR(255) NOT NULL   COMMENT '模型url' ,
                             `adv_settings` VARCHAR(500) NOT NULL   COMMENT '模型参数配置值' ,
                             `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                             `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人(操作人）' ,
                             PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '模型对接保存参数';

CREATE INDEX idx_type ON ai_model_source(`type`);
CREATE INDEX idx_owner ON ai_model_source(`owner`);


-- ai 对话
CREATE TABLE IF NOT EXISTS ai_conversation(
    `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
    `title` VARCHAR(255) NOT NULL   COMMENT '对话标题' ,
    `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
    `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人(操作人）' ,
    PRIMARY KEY (id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci COMMENT = 'AI对话';

CREATE INDEX idx_create_user ON ai_conversation(`create_user`);

-- ai 对话内容
CREATE TABLE IF NOT EXISTS ai_conversation_content(
`id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
`conversation_id` VARCHAR(50) NOT NULL   COMMENT '对话ID' ,
`type` VARCHAR(10) NOT NULL   COMMENT '记录类型（USER, ASSISTANT, SYSTEM, TOOL）' ,
`content` TEXT NOT NULL   COMMENT '对话标题' ,
`create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
PRIMARY KEY (id)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_general_ci COMMENT = 'AI对话内容';

CREATE INDEX idx_conversation_create_time ON ai_conversation_content(`conversation_id`,`create_time` DESC);

alter table functional_case
    add ai_create bit default b'0' not null comment '是否是ai自动生成的用例：0-否，1-是';

-- 用户AI提示词配置表
CREATE TABLE IF NOT EXISTS  ai_user_prompt_config(
                                      `id` VARCHAR(50) NOT NULL   COMMENT 'id' ,
                                      `user_id` VARCHAR(50) NOT NULL   COMMENT '用户id' ,
                                      `type` VARCHAR(50) NOT NULL   COMMENT '配置类型（API/CASE/BUG）' ,
                                      `config` LONGBLOB NOT NULL   COMMENT '配置内容' ,
                                      PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户Ai提示词配置表';

CREATE INDEX idx_user_id ON ai_user_prompt_config(`user_id`);
CREATE INDEX idx_type ON ai_user_prompt_config(`type`);

alter table api_test_case
    add ai_create bit default b'0' not null comment '是否是ai自动生成的用例：0-否，1-是';
-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

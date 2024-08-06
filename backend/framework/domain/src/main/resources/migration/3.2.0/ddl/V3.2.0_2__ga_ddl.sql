-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE api_test_case ADD api_change BIT(1) DEFAULT 0 NOT NULL COMMENT '接口定义参数变更标识';
ALTER TABLE api_test_case ADD ignore_api_change BIT(1) DEFAULT 0 NOT NULL COMMENT '忽略接口定义参数变更';
ALTER TABLE api_test_case ADD ignore_api_diff BIT(1) DEFAULT 0 NOT NULL COMMENT '忽略接口与用例参数不一致';

CREATE TABLE export_task(
                            `id` VARCHAR(50) NOT NULL   COMMENT '任务唯一ID' ,
                            `name` VARCHAR(255)    COMMENT '名称' ,
                            `type` VARCHAR(50) NOT NULL   COMMENT '资源类型' ,
                            `fileId` VARCHAR(255)    COMMENT '文件id' ,
                            `state` VARCHAR(50) NOT NULL   COMMENT '状态' ,
                            `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                            `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                            PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '导出任务';


CREATE INDEX idx_create_user ON export_task(`create_user`);
CREATE INDEX idx_state ON export_task(`state`);
CREATE INDEX idx_create_time ON export_task(`create_time`);
CREATE INDEX idx_type ON export_task(`type`);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;




-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

DROP TABLE IF EXISTS platform_source;
CREATE TABLE platform_source(
                                `platform` VARCHAR(50) NOT NULL   COMMENT '平台名称（国际飞书:LARK_SUITE，飞书:LARK，钉钉:DING_TALK，企业微信:WE_COM）' ,
                                `config` BLOB NOT NULL   COMMENT '平台信息配置' ,
                                `enable` BIT NOT NULL  DEFAULT 1 COMMENT '是否开启' ,
                                `valid` BIT NOT NULL  DEFAULT 0 COMMENT '名称' ,
                                PRIMARY KEY (platform)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '平台对接保存参数';


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;



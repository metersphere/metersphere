SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE quota
    ADD module_setting varchar(100) COMMENT '模块设置';

SET SESSION innodb_lock_wait_timeout = DEFAULT;

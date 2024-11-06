-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 缺陷自定义字段增加文本字段
ALTER TABLE bug_custom_field ADD COLUMN `content` VARCHAR(1000) COMMENT '字段文本';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

SET SESSION innodb_lock_wait_timeout = 7200;

-- 初始化默认参数文件上传大小
INSERT INTO system_parameter (param_key, param_value, type, sort) VALUES ('base.file.upload.size', '50', 'text', 1);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

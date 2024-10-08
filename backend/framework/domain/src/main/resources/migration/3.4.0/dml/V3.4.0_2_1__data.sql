-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 定时任务历史数据num值处理
UPDATE `schedule` set num = UUID_SHORT() % 10000000

-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 定时任务历史数据num值处理
UPDATE `schedule` set num = UUID_SHORT() % 10000000;

-- 定时任务历史数据name处理
update `schedule` set name = '定时同步缺陷' where name = 'Bug Sync Job';
update `schedule` set name = '定时同步需求' where name = 'Demand Sync Job';

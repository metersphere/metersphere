SET SESSION innodb_lock_wait_timeout = 7200;

-- 由于更新了SendReportJob类的包，所以针对之前设置好的定时任务，要修改它们对应的job
UPDATE `schedule` SET job = 'io.metersphere.reportstatistics.job' WHERE job = 'io.metersphere.xpack.reportstatistics.job.SendReportJob';

SET SESSION innodb_lock_wait_timeout = DEFAULT;
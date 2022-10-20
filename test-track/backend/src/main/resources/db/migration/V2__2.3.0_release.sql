-- 同步缺陷定时器修改
update schedule set value = '0 0 0 * * ?', job = 'io.metersphere.job.schedule.IssueSyncJob' where id = '7a23d4db-9909-438d-9e36-58e432c8c4ae'

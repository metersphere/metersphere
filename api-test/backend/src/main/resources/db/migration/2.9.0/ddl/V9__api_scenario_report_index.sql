SET SESSION innodb_lock_wait_timeout = 7200;

-- 增加一个索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_scenario_report'
                   AND index_name LIKE 'api_scenario_report_create_time_index'),
          'select 1',
          'ALTER TABLE api_scenario_report ADD INDEX api_scenario_report_create_time_index (create_time)')
INTO @a;
PREPARE createTime FROM @a;
EXECUTE createTime;
DEALLOCATE PREPARE createTime;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
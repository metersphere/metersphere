SET SESSION innodb_lock_wait_timeout = 7200;

--
-- 增加一个索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_scenario'
                   AND index_name LIKE 'api_scenario_module_id_index'),
          'select 1',
          'ALTER TABLE api_scenario ADD INDEX api_scenario_module_id_index (api_scenario_module_id)')
INTO @a;
PREPARE moduleId FROM @a;
EXECUTE moduleId;
DEALLOCATE PREPARE moduleId;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
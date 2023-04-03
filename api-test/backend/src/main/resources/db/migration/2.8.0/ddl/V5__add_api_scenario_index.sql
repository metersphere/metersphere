SET SESSION innodb_lock_wait_timeout = 7200;

-- 增加一个场景模块索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_scenario'
                   AND index_name LIKE 'api_scenario_module_id_index'),
          'select 1',
          'ALTER TABLE api_scenario ADD INDEX api_scenario_module_id_index (api_scenario_module_id)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- 增加一个接口定义协议索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_definition'
                   AND index_name LIKE 'api_definition_protocol_index'),
          'select 1',
          'ALTER TABLE api_definition ADD INDEX api_definition_protocol_index (protocol)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_test_case'
                   AND index_name LIKE 'api_test_case_project_id_index'),
          'select 1',
          'ALTER TABLE api_test_case ADD INDEX api_test_case_project_id_index (project_id)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
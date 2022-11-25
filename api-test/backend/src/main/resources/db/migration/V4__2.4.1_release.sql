--
-- 增加一个索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_definition'
                   AND index_name LIKE 'api_definition_module_id_status_index'),
          'select 1',
          'ALTER TABLE api_definition ADD INDEX api_definition_module_id_status_index (module_id, status)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;
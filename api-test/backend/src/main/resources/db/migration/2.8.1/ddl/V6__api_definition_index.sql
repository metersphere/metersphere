SET SESSION innodb_lock_wait_timeout = 7200;

--
-- 增加一个索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_definition'
                   AND index_name LIKE 'api_definition_protocol_index'),
          'select 1',
          'ALTER TABLE api_definition ADD INDEX api_definition_protocol_index (protocol)')
INTO @a;
PREPARE protocol FROM @a;
EXECUTE protocol;
DEALLOCATE PREPARE protocol;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
SET SESSION innodb_lock_wait_timeout = 7200;

-- 删除v1.20.14版本使用，2.9不再使用的字段
SELECT IF(
               EXISTS(SELECT DISTINCT column_name
                      FROM information_schema.columns
                      WHERE table_schema = DATABASE()
                        AND table_name = 'api_scenario_report'
                        AND column_name = 'md5_scenario_id'),
               'ALTER TABLE api_scenario_report DROP COLUMN md5_scenario_id',
               'select 1'
           )
INTO @a;
PREPARE dropColumn FROM @a;
EXECUTE dropColumn;
DEALLOCATE PREPARE dropColumn;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
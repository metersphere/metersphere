SET SESSION innodb_lock_wait_timeout = 7200;

--
-- 增加一个索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'api_test_case'
                   AND index_name LIKE 'api_test_case_project_id_index'),
          'select 1',
          'ALTER TABLE api_test_case ADD INDEX api_test_case_project_id_index (project_id)')
INTO @a;
PREPARE projectId FROM @a;
EXECUTE projectId;
DEALLOCATE PREPARE projectId;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
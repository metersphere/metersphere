SET SESSION innodb_lock_wait_timeout = 7200;


SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'test_case_node'
                   AND index_name LIKE 'test_case_node_parent_id_index'),
          'select 1',
          'ALTER TABLE test_case_node ADD INDEX test_case_node_parent_id_index (parent_id)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'test_case_node'
                   AND index_name LIKE 'test_case_node_name_index'),
          'select 1',
          'ALTER TABLE test_case_node ADD INDEX test_case_node_name_index (name)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'test_case_node'
                   AND index_name LIKE 'test_case_node_level_index'),
          'select 1',
          'ALTER TABLE test_case_node ADD INDEX test_case_node_level_index (level)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET SESSION innodb_lock_wait_timeout = DEFAULT;
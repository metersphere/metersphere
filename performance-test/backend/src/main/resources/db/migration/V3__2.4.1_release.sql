--
-- 增加一个索引
SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'load_test_report_detail'
                   AND index_name LIKE 'load_test_report_detail_report_id_part_index'),
          'select 1',
          'ALTER TABLE load_test_report_detail ADD INDEX load_test_report_detail_report_id_part_index (report_id, part)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;
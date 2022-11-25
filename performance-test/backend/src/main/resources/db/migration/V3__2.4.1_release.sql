--
ALTER TABLE load_test_report_detail ADD INDEX load_test_report_detail_report_id_part_index (report_id, part);
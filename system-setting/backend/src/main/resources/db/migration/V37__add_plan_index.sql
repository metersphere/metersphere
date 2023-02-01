SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE `test_plan_test_case` ADD INDEX index_name ( `case_id` );
ALTER TABLE `test_case_review_test_case` ADD INDEX index_name ( `case_id` );
SET SESSION innodb_lock_wait_timeout = DEFAULT;

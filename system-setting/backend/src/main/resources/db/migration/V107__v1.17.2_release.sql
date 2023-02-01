SET SESSION innodb_lock_wait_timeout = 7200;
alter table api_execution_queue_detail add index queue_id_test_id_index(queue_id,test_id);
SET SESSION innodb_lock_wait_timeout = DEFAULT;

SET SESSION innodb_lock_wait_timeout = 7200;
update api_scenario set custom_num = num where (custom_num is null or custom_num = '');
SET SESSION innodb_lock_wait_timeout = DEFAULT;

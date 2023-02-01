SET SESSION innodb_lock_wait_timeout = 7200;

-- 更改状态
update enterprise_test_report set status ='SEND_FAILED' where status = 'SEND_FAILD';

SET SESSION innodb_lock_wait_timeout = DEFAULT;
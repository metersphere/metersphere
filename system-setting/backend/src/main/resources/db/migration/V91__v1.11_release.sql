SET SESSION innodb_lock_wait_timeout = 7200;
-- v1.10-lts 升级的基线是 v92，v91不会执行，这里把v91的内容加到这里，保证lts可以正常升级
SELECT DATABASE();
SET SESSION innodb_lock_wait_timeout = DEFAULT;

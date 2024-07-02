-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

DROP TABLE IF EXISTS functional_minder_extra_node;


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
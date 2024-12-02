-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE INDEX idx_type_project_id
    ON test_plan (type, project_id);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

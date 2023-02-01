SET SESSION innodb_lock_wait_timeout = 7200;
ALTER TABLE swagger_url_project
    MODIFY COLUMN id VARCHAR(120);
ALTER TABLE swagger_url_project
    MODIFY COLUMN project_id VARCHAR(120);
ALTER TABLE swagger_url_project
    MODIFY COLUMN mode_id VARCHAR(120);

SET SESSION innodb_lock_wait_timeout = DEFAULT;

-- test resource pool
ALTER TABLE test_resource_pool
    ADD api TINYINT(1) NULL;

ALTER TABLE test_resource_pool
    ADD performance TINYINT(1) NULL;

UPDATE test_resource_pool
SET api         = TRUE,
    performance = TRUE
WHERE type = 'NODE';

UPDATE test_resource_pool
SET api         = FALSE,
    performance = TRUE
WHERE type = 'K8S';

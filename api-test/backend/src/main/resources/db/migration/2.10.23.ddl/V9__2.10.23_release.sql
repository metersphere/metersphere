SET SESSION innodb_lock_wait_timeout = 7200;

CREATE INDEX idx_api_scenario_num ON api_scenario (num DESC);

SET SESSION innodb_lock_wait_timeout = DEFAULT;
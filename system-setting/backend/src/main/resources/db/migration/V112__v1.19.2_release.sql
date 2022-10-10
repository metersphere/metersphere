ALTER TABLE test_plan
    MODIFY name VARCHAR(128) NOT NULL COMMENT 'Plan name';

DROP PROCEDURE IF EXISTS schema_change_api;
DELIMITER //
CREATE PROCEDURE schema_change_api() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_test_case' AND index_name = 'plan_id_index') THEN
        ALTER TABLE `test_plan_test_case` ADD INDEX plan_id_index ( `plan_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_api();



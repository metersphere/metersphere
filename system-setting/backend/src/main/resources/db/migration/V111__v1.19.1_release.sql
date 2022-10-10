
DROP PROCEDURE IF EXISTS schema_change_api;
DELIMITER //
CREATE PROCEDURE schema_change_api() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_api_case' AND index_name = 'plan_id_index') THEN
        ALTER TABLE `test_plan_api_case` ADD INDEX plan_id_index ( `test_plan_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_api();

DROP PROCEDURE IF EXISTS schema_change_api_one;
DELIMITER //
CREATE PROCEDURE schema_change_api_one() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF  EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_api_case' AND index_name = 'planIdIndex') THEN
        ALTER TABLE `test_plan_api_case` DROP INDEX planIdIndex;
    END IF;
END//
DELIMITER ;
CALL schema_change_api_one();

DROP PROCEDURE IF EXISTS schema_change_scenario;
DELIMITER //
CREATE PROCEDURE schema_change_scenario() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_api_scenario' AND index_name = 'plan_id_index') THEN
        ALTER TABLE `test_plan_api_scenario` ADD INDEX plan_id_index ( `test_plan_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_scenario();

DROP PROCEDURE IF EXISTS schema_change_scenario_one;
DELIMITER //
CREATE PROCEDURE schema_change_scenario_one() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF  EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_api_scenario' AND index_name = 'planIdIndex') THEN
        ALTER TABLE `test_plan_api_scenario` DROP INDEX planIdIndex;
    END IF;
END//
DELIMITER ;
CALL schema_change_scenario_one();


DROP PROCEDURE IF EXISTS schema_change_load;
DELIMITER //
CREATE PROCEDURE schema_change_load() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_load_case' AND index_name = 'plan_id_index') THEN
        ALTER TABLE `test_plan_load_case` ADD INDEX plan_id_index ( `test_plan_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_load();

DROP PROCEDURE IF EXISTS schema_change_load_one;
DELIMITER //
CREATE PROCEDURE schema_change_load_one() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF  EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_load_case' AND index_name = 'planIdIndex') THEN
        ALTER TABLE `test_plan_load_case` DROP INDEX planIdIndex;
    END IF;
END//
DELIMITER ;
CALL schema_change_load_one();


DROP PROCEDURE IF EXISTS schema_change_report;
DELIMITER //
CREATE PROCEDURE schema_change_report() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_report' AND index_name = 'plan_id_index') THEN
        ALTER TABLE `test_plan_report` ADD INDEX plan_id_index ( `test_plan_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_report();

DROP PROCEDURE IF EXISTS schema_change_report_one;
DELIMITER //
CREATE PROCEDURE schema_change_report_one() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF  EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_plan_report' AND index_name = 'planIdIndex') THEN
        ALTER TABLE `test_plan_report` DROP INDEX planIdIndex;
    END IF;
END//
DELIMITER ;
CALL schema_change_report_one();


DROP PROCEDURE IF EXISTS schema_change_issue;
DELIMITER //
CREATE PROCEDURE schema_change_issue() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'test_case_issues' AND index_name = 'issues_id_index') THEN
        ALTER TABLE `test_case_issues` ADD INDEX issues_id_index ( `issues_id` );
    END IF;
END//
DELIMITER ;
CALL schema_change_issue();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
    DECLARE  CurrentDatabase VARCHAR(100);
    SELECT DATABASE() INTO CurrentDatabase;
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema=CurrentDatabase AND table_name = 'api_scenario_report' AND index_name = 'update_time_index') THEN
        ALTER TABLE `api_scenario_report` ADD INDEX update_time_index ( `update_time` );
    END IF;
END//
DELIMITER ;
CALL schema_change();

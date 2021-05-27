-- project add column
ALTER table project add system_id varchar(50) null;
-- reduse old data
DROP PROCEDURE IF EXISTS project_systemid;
DELIMITER //
CREATE PROCEDURE project_systemid()
BEGIN
    DECLARE projectId VARCHAR(64);
    DECLARE num INT;
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT DISTINCT id
                                FROM project
                                WHERE system_id IS NULL);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    SET num = 100001;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO projectId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        UPDATE project
        SET system_id = num
        WHERE id = projectId;
        SET num = num + 1;
        SET done = 0;
    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL project_systemid();
DROP PROCEDURE IF EXISTS project_systemid;
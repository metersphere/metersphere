alter table issues add num int null;

DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE projectId VARCHAR(64);
    DECLARE issueId VARCHAR(64);
    DECLARE num INT;
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT DISTINCT project_id
                                FROM issues
                                WHERE num IS NULL);
    DECLARE cursor2 CURSOR FOR (SELECT id
                                FROM issues
                                WHERE project_id = projectId
                                ORDER BY create_time);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO projectId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        SET num = 100001;
        OPEN cursor2;
        inner_loop:
        LOOP
            FETCH cursor2 INTO issueId;
            IF done
            THEN
                LEAVE inner_loop;
            END IF;
            UPDATE issues
            SET num = num
            WHERE id = issueId;
            SET num = num + 1;
        END LOOP;
        SET done = 0;
        CLOSE cursor2;
    END LOOP;
    CLOSE cursor1;
END //
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;
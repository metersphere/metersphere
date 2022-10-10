-- 初始化 sql
-- 创建 性能测试报告和文件的关联
-- start
CREATE TABLE `load_test_report_file`
(
    `report_id` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
    `file_id`   varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
    `sort`      int(11)                         DEFAULT NULL,
    UNIQUE KEY `load_test_report_file_pk` (`report_id`, `file_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 处理历史数据，把测试的关联文件和报告的补上
DROP PROCEDURE IF EXISTS load_test_report_file_add;
DELIMITER //
CREATE PROCEDURE load_test_report_file_add()
BEGIN
    DECLARE testId varchar(64);
    DECLARE fileId varchar(64);
    DECLARE sort int;

    DECLARE done INT DEFAULT 0;
    #声明游标
    DECLARE file_cur CURSOR FOR SELECT load_test_file.test_id, load_test_file.file_id, load_test_file.sort
                                FROM load_test_file;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN file_cur;
    outer_loop:
    LOOP
        FETCH file_cur INTO testId, fileId, sort;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        INSERT INTO load_test_report_file(report_id, file_id, sort)
        SELECT id, fileId, sort
        FROM load_test_report
        WHERE test_id = testId;
    END LOOP;

    #关闭游标
    CLOSE file_cur;
END
//
DELIMITER ;

CALL load_test_report_file_add();
DROP PROCEDURE IF EXISTS load_test_report_file_add;
-- end
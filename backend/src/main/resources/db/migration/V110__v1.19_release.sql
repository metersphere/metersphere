-- module management
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('metersphere.module.workstation', 'ENABLE', 'text', 1);

DROP PROCEDURE IF EXISTS project_api_appl;
DELIMITER //
CREATE PROCEDURE project_api_appl()
BEGIN
    #声明结束标识
    DECLARE end_flag int DEFAULT 0;

    DECLARE projectId varchar(64);

    #声明游标 group_curosr
    DECLARE project_curosr CURSOR FOR SELECT DISTINCT id FROM project;

#设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET end_flag = 1;

    #打开游标
    OPEN project_curosr;
    #获取当前游标指针记录，取出值赋给自定义的变量
    FETCH project_curosr INTO projectId;
    #遍历游标
    REPEAT
        #利用取到的值进行数据库的操作
        INSERT INTO project_application (project_id, type, type_value)
        VALUES (projectId, 'API_SHARE_REPORT_TIME', '24H');
        # 将游标中的值再赋值给变量，供下次循环使用
        FETCH project_curosr INTO projectId;
    UNTIL end_flag END REPEAT;

    #关闭游标
    CLOSE project_curosr;

END
//
DELIMITER ;

CALL project_api_appl();
DROP PROCEDURE IF EXISTS project_api_appl;

ALTER TABLE `api_definition` ADD INDEX methodIndex ( `method` );
ALTER TABLE `api_definition` ADD INDEX protocolIndex ( `protocol` );
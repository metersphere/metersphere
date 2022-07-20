--
-- V2_sync_supplementary_data
-- 查出所有不重复的项目ID 循环项目ID 给 project_application 表设置默认值
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
        DELETE FROM project_application WHERE project_id = projectId AND type = 'TRIGGER_UPDATE';
        INSERT INTO project_application (project_id, type, type_value)
        VALUES (projectId, 'TRIGGER_UPDATE',
                '{"protocol":true,"method":true,"path":true,"headers":true,"query":true,"rest":true,"body":true,"delNotSame":true,"runError":true,"unRun":true,"ids":null}');
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

--
-- jianguo

DROP PROCEDURE IF EXISTS init_api_execution;

DELIMITER //
CREATE PROCEDURE init_api_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(64);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);

    DECLARE reports CURSOR FOR
        SELECT api_definition_exec_result.resource_id,
               api_definition_exec_result.`status`,
               api_definition_exec_result.create_time
        FROM api_definition_exec_result
                 INNER JOIN api_definition ON api_definition_exec_result.resource_id = api_definition.id
        WHERE api_definition_exec_result.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_definition_exec_result.create_time IS NOT NULL;

    #设置终止标志    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN reports;
    outer_loop:
    LOOP
        #获取当前游标指针记录，取出值赋给自定义的变量
        FETCH reports INTO id, `status`, create_time;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO api_execution_info (id, source_id, create_time, result)
        VALUES (UUID(), `id`, create_time, `status`);
    END LOOP;
    #关闭游标
    CLOSE reports;
END
//
DELIMITER ;
CALL init_api_execution();
DROP PROCEDURE IF EXISTS init_api_execution;


DROP PROCEDURE IF EXISTS init_scenario_execution;
DELIMITER //
CREATE PROCEDURE init_scenario_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(255);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);
    DECLARE `trigger_mode` varchar(50);
    DECLARE scenario_report CURSOR FOR
        SELECT DISTINCT api_scenario_report.scenario_id,
                        api_scenario_report.`status`,
                        api_scenario_report.create_time,
                        api_scenario_report.trigger_mode
        FROM api_scenario_report
                 INNER JOIN api_scenario ON api_scenario_report.scenario_id = api_scenario.id
        WHERE api_scenario_report.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_scenario_report.`status` IS NOT NULL
          AND api_scenario_report.`trigger_mode` IS NOT NULL;

    #设置终止标志    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN scenario_report;
    outer_loop:
    LOOP
        #获取当前游标指针记录取出值赋给自定义的变量
        FETCH scenario_report INTO id, `status`, create_time,`trigger_mode`;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO scenario_execution_info (id, source_id, create_time, result, trigger_mode)
        VALUES (UUID(), `id`, create_time, `status`, `trigger_mode`);
    END LOOP;
    #关闭游标
    CLOSE scenario_report;
END
//
DELIMITER ;

CALL init_scenario_execution();
DROP PROCEDURE IF EXISTS init_scenario_execution;


DROP PROCEDURE IF EXISTS init_api_case_execution;
DELIMITER //
CREATE PROCEDURE init_api_case_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(64);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);
    DECLARE `trigger_mode` varchar(50);

    DECLARE caseReports CURSOR FOR
        SELECT api_definition_exec_result.resource_id,
               api_definition_exec_result.`status`,
               api_definition_exec_result.create_time,
               api_definition_exec_result.trigger_mode
        FROM api_definition_exec_result
                 INNER JOIN api_test_case ON api_definition_exec_result.resource_id = api_test_case.id
        WHERE api_definition_exec_result.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_definition_exec_result.create_time IS NOT NULL
          AND api_definition_exec_result.trigger_mode IS NOT NULL;
    #设置终止标志    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN caseReports;
    outer_loop:
    LOOP
        #获取当前游标指针记录，取出值赋给自定义的变量
        FETCH caseReports INTO id, `status`, create_time,`trigger_mode`;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO api_case_execution_info (id, source_id, create_time, result, trigger_mode)
        VALUES (UUID(), `id`, create_time, `status`, `trigger_mode`);
    END LOOP;
    #关闭游标
    CLOSE caseReports;

END
//
DELIMITER ;
CALL init_api_case_execution();
DROP PROCEDURE IF EXISTS init_api_case_execution;



DROP PROCEDURE IF EXISTS init_plan_case_execution;
DELIMITER //
CREATE PROCEDURE init_plan_case_execution()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE id varchar(64);
    DECLARE `status` varchar(64);
    DECLARE create_time bigint(13);
    DECLARE `trigger_mode` varchar(50);

    DECLARE caseReports CURSOR FOR
        SELECT api_definition_exec_result.resource_id,
               api_definition_exec_result.`status`,
               api_definition_exec_result.create_time,
               api_definition_exec_result.trigger_mode
        FROM api_definition_exec_result
                 INNER JOIN test_plan_api_case ON api_definition_exec_result.resource_id = test_plan_api_case.id
                 INNER JOIN api_test_case ON test_plan_api_case.api_case_id = api_test_case.id
        WHERE api_definition_exec_result.`status` NOT IN ('Running', 'Waiting', 'Timeout')
          AND api_definition_exec_result.create_time IS NOT NULL
          AND api_definition_exec_result.trigger_mode IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    #打开游标
    OPEN caseReports;
    outer_loop:
    LOOP
        #获取当前游标指针记录，取出值赋给自定义的变量
        FETCH caseReports INTO id, `status`, create_time,`trigger_mode`;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        #利用取到的值进行数据库的操作
        INSERT INTO api_case_execution_info (id, source_id, create_time, result, trigger_mode)
        VALUES (UUID(), `id`, create_time, `status`, `trigger_mode`);
    END LOOP;
    #关闭游标
    CLOSE caseReports;

END
//
DELIMITER ;
CALL init_plan_case_execution();
DROP PROCEDURE IF EXISTS init_plan_case_execution;

-- 初始化 last_execute_result
UPDATE test_case, test_plan_test_case
SET test_case.last_execute_result = test_plan_test_case.status
WHERE test_case.id = test_plan_test_case.case_id;


--
-- V125_2-0-0_test_case_comment
UPDATE test_plan_test_case
SET status = 'Prepare'
WHERE status = 'Underway';

UPDATE test_case_comment
SET `type` = 'REVIEW'
WHERE status IN ('UnPass', 'Pass');

UPDATE test_case_comment
SET `type` = 'PLAN'
WHERE status IN ('Prepare', 'Underway', 'Failure', 'Blocking', 'Skip');

UPDATE test_case_comment
SET `type` = 'CASE'
WHERE `type` = '';

--
-- 初始化attachment_module_relation数据
INSERT INTO attachment_module_relation SELECT case_id, 'test_case', file_id FROM test_case_file;
-- 清空test_case_file表数据
DELETE FROM test_case_file;


--
-- v2_init_permissions
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TRACK_CASE:READ+BATCH_LINK_DEMAND', 'PROJECT_TRACK_CASE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_TRACK_CASE:READ+BATCH_LINK_DEMAND', 'PROJECT_TRACK_CASE');

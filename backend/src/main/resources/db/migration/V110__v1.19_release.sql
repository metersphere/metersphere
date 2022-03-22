
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
        DELETE FROM project_application WHERE project_id = projectId AND type = 'API_SHARE_REPORT_TIME';
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


--
ALTER TABLE message_task
    ADD project_id VARCHAR(64) NULL;

-- 消息通知去掉工作空间
DROP PROCEDURE IF EXISTS message_task_ws;
DELIMITER //
CREATE PROCEDURE message_task_ws()
BEGIN
    DECLARE userId VARCHAR(64);
    DECLARE testId VARCHAR(64);
    DECLARE type VARCHAR(64);
    DECLARE event VARCHAR(64);
    DECLARE taskType VARCHAR(64);
    DECLARE webhook VARCHAR(255);
    DECLARE identification VARCHAR(64);
    DECLARE isSet VARCHAR(64);
    DECLARE workspaceId VARCHAR(64);
    DECLARE createTime BIGINT;
    DECLARE template TEXT;

    DECLARE done INT DEFAULT 0;
    # 必须用 table_name.column_name
    DECLARE cursor1 CURSOR FOR SELECT message_task.type,
                                      message_task.event,
                                      message_task.user_id,
                                      message_task.task_type,
                                      message_task.webhook,
                                      message_task.identification,
                                      message_task.is_set,
                                      message_task.workspace_id,
                                      message_task.test_id,
                                      message_task.create_time,
                                      message_task.template
                               FROM message_task;


    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO type, event, userId, taskType, webhook, identification, isSet,
            workspaceId,
            testId, createTime, template;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        INSERT INTO message_task(id, type, event, user_id, task_type, webhook, identification, is_set, project_id,
                                 test_id, create_time, template)
        SELECT UUID(),
               type,
               event,
               userId,
               taskType,
               webhook,
               identification,
               isSet,
               id,
               testId,
               createTime,
               template
        FROM project
        WHERE workspace_id = workspaceId;
        DELETE FROM message_task WHERE workspace_id = workspaceId;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL message_task_ws();
DROP PROCEDURE IF EXISTS message_task_ws;
-- 去掉组织id
ALTER TABLE message_task
    DROP COLUMN workspace_id;

-- 默认设置权限
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_MESSAGE:READ', 'PROJECT_MESSAGE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_MESSAGE:READ+EDIT', 'PROJECT_MESSAGE');

--
ALTER TABLE custom_field
    ADD project_id VARCHAR(64) NULL;
ALTER TABLE test_case_template
    ADD project_id VARCHAR(64) NULL;
ALTER TABLE issue_template
    ADD project_id VARCHAR(64) NULL;

ALTER TABLE custom_field
    MODIFY id VARCHAR(100);
ALTER TABLE test_case_template
    MODIFY id VARCHAR(100);
ALTER TABLE issue_template
    MODIFY id VARCHAR(100);

UPDATE custom_field
SET project_id = 'global'
WHERE global = 1;

UPDATE test_case_template
SET project_id = 'global'
WHERE global = 1;

UPDATE issue_template
SET project_id = 'global'
WHERE global = 1;


-- 自定义字段去掉工作空间
DROP PROCEDURE IF EXISTS custom_field_ws;
DELIMITER //
CREATE PROCEDURE custom_field_ws()
BEGIN

    DECLARE fieldId VARCHAR(64);
    DECLARE name VARCHAR(64);
    DECLARE scene VARCHAR(64);
    DECLARE type VARCHAR(64);
    DECLARE remark VARCHAR(255);
    DECLARE options TEXT;
    DECLARE `system` TINYINT(1);
    DECLARE global TINYINT(1);
    DECLARE workspaceId VARCHAR(64);
    DECLARE createTime BIGINT;
    DECLARE updateTime BIGINT;
    DECLARE createUser VARCHAR(64);

    DECLARE done INT DEFAULT 0;
    # 必须用 table_name.column_name
    DECLARE cursor1 CURSOR FOR SELECT custom_field.id,
                                      custom_field.name,
                                      custom_field.scene,
                                      custom_field.type,
                                      custom_field.remark,
                                      custom_field.options,
                                      custom_field.`system`,
                                      custom_field.global,
                                      custom_field.workspace_id,
                                      custom_field.create_time,
                                      custom_field.update_time,
                                      custom_field.create_user
                               FROM custom_field
                               WHERE custom_field.global = 0;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO fieldId, name, scene, type, remark, options, `system`, global, workspaceId,
            createTime, updateTime, createUser;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;

        -- 自定义字段数据下发到项目
        INSERT INTO custom_field (id, name, scene, type, remark, options, `system`, global, create_time,
                                  update_time, create_user, project_id, workspace_id)
        SELECT CONCAT(fieldId, '-', SUBSTRING(MD5(RAND()), 1, 10)),
               custom_field.name,
               scene,
               type,
               remark,
               options,
               `system`,
               global,
               custom_field.create_time,
               custom_field.update_time,
               custom_field.create_user,
               project.id,
               project.workspace_id
        FROM project
                 JOIN custom_field ON project.workspace_id = custom_field.workspace_id
        WHERE custom_field.id = fieldId;
        -- 删除处理过的数据
        DELETE FROM custom_field WHERE id = fieldId;

    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL custom_field_ws();
DROP PROCEDURE IF EXISTS custom_field_ws;


-- 用例模版去掉工作空间
DROP PROCEDURE IF EXISTS case_template_ws;
DELIMITER //
CREATE PROCEDURE case_template_ws()
BEGIN

    DECLARE templateId VARCHAR(64);
    DECLARE name VARCHAR(64);
    DECLARE type VARCHAR(64);
    DECLARE description VARCHAR(64);
    DECLARE caseName VARCHAR(255);
    DECLARE `system` TINYINT(1);
    DECLARE global TINYINT(1);
    DECLARE workspaceId VARCHAR(64);
    DECLARE prerequisite VARCHAR(255);
    DECLARE stepDescription TEXT;
    DECLARE expectedResult TEXT;
    DECLARE actualResult TEXT;
    DECLARE createTime BIGINT;
    DECLARE updateTime BIGINT;
    DECLARE stepModel VARCHAR(10);
    DECLARE steps TEXT;
    DECLARE createUser VARCHAR(64);

    DECLARE done INT DEFAULT 0;
    # 必须用 table_name.column_name
    DECLARE cursor1 CURSOR FOR SELECT test_case_template.id,
                                      test_case_template.name,
                                      test_case_template.type,
                                      test_case_template.description,
                                      test_case_template.case_name,
                                      test_case_template.`system`,
                                      test_case_template.global,
                                      test_case_template.workspace_id,
                                      test_case_template.prerequisite,
                                      test_case_template.step_description,
                                      test_case_template.expected_result,
                                      test_case_template.actual_result,
                                      test_case_template.create_time,
                                      test_case_template.update_time,
                                      test_case_template.step_model,
                                      test_case_template.steps,
                                      test_case_template.create_user
                               FROM test_case_template
                               WHERE test_case_template.global = 0;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO templateId, name, type, description, caseName, `system`, global, workspaceId,
            prerequisite, stepDescription, expectedResult, actualResult, createTime,
            updateTime, stepModel, steps, createUser;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;

        -- 自定义字段数据下发到项目
        INSERT INTO test_case_template (id, name, type, description, case_name, `system`, global, workspace_id,
                                        prerequisite, step_description, expected_result, actual_result, create_time,
                                        update_time, step_model, steps, create_user, project_id)
        SELECT CONCAT(templateId, '-', SUBSTRING(MD5(RAND()), 1, 10)),
               test_case_template.name,
               type,
               test_case_template.description,
               case_name,
               `system`,
               global,
               test_case_template.workspace_id,
               prerequisite,
               step_description,
               expected_result,
               actual_result,
               test_case_template.create_time,
               test_case_template.update_time,
               step_model,
               steps,
               test_case_template.create_user,
               project.id
        FROM project
                 JOIN test_case_template ON project.workspace_id = test_case_template.workspace_id
        WHERE test_case_template.id = templateId;
        -- 删除处理过的数据
        DELETE FROM test_case_template WHERE id = templateId;

    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL case_template_ws();
DROP PROCEDURE IF EXISTS case_template_ws;


-- 缺陷模版去掉工作空间
DROP PROCEDURE IF EXISTS issue_template_ws;
DELIMITER //
CREATE PROCEDURE issue_template_ws()
BEGIN

    DECLARE templateId VARCHAR(64);
    DECLARE name VARCHAR(64);
    DECLARE platform VARCHAR(64);
    DECLARE description VARCHAR(64);
    DECLARE title VARCHAR(255);
    DECLARE `system` TINYINT(1);
    DECLARE global TINYINT(1);
    DECLARE workspaceId VARCHAR(64);
    DECLARE content TEXT;
    DECLARE createTime BIGINT;
    DECLARE updateTime BIGINT;
    DECLARE createUser VARCHAR(64);

    DECLARE done INT DEFAULT 0;
    # 必须用 table_name.column_name
    DECLARE cursor1 CURSOR FOR SELECT issue_template.id,
                                      issue_template.name,
                                      issue_template.platform,
                                      issue_template.description,
                                      issue_template.title,
                                      issue_template.`system`,
                                      issue_template.global,
                                      issue_template.workspace_id,
                                      issue_template.content,
                                      issue_template.create_time,
                                      issue_template.update_time,
                                      issue_template.create_user
                               FROM issue_template
                               WHERE issue_template.global = 0;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO templateId, name, platform, description, title, `system`, global, workspaceId, content,
            createTime, updateTime, createUser;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;

        -- 自定义字段数据下发到项目
        INSERT INTO issue_template(id, name, platform, description, title, `system`, global, content,
                                   create_time, update_time, create_user, project_id, workspace_id)
        SELECT CONCAT(templateId, '-', SUBSTRING(MD5(RAND()), 1, 10)),
               issue_template.name,
               issue_template.platform,
               issue_template.description,
               title,
               `system`,
               global,
               content,
               issue_template.create_time,
               issue_template.update_time,
               issue_template.create_user,
               project.id,
               issue_template.workspace_id
        FROM project
                 JOIN issue_template ON project.workspace_id = issue_template.workspace_id
        WHERE issue_template.id = templateId;
        -- 删除处理过的数据
        DELETE FROM issue_template WHERE id = templateId;

    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL issue_template_ws();
DROP PROCEDURE IF EXISTS issue_template_ws;


DROP TABLE IF EXISTS custom_field_template_bak;
CREATE TEMPORARY TABLE custom_field_template_bak LIKE custom_field_template;
INSERT INTO custom_field_template_bak
SELECT *
FROM custom_field_template;
TRUNCATE TABLE custom_field_template;

-- 关系表去掉工作空间
DROP PROCEDURE IF EXISTS custom_field_template_ws;
DELIMITER //
CREATE PROCEDURE custom_field_template_ws()
BEGIN

    DECLARE fieldId VARCHAR(64);
    DECLARE templateId VARCHAR(64);
    DECLARE scene VARCHAR(64);
    DECLARE required TINYINT(1);
    DECLARE `order` INT;
    DECLARE defaultValue VARCHAR(100);
    DECLARE customData VARCHAR(255);
    DECLARE `key` VARCHAR(1);

    DECLARE done INT DEFAULT 0;
    # 必须用 table_name.column_name
    DECLARE cursor1 CURSOR FOR SELECT custom_field_template_bak.field_id,
                                      custom_field_template_bak.template_id,
                                      custom_field_template_bak.scene,
                                      custom_field_template_bak.required,
                                      custom_field_template_bak.`order`,
                                      custom_field_template_bak.default_value,
                                      custom_field_template_bak.custom_data,
                                      custom_field_template_bak.`key`
                               FROM custom_field_template_bak;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO fieldId, templateId, scene, required, `order`, defaultValue, customData, `key`;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;

        #
        -- 关联数据下发到项目
        INSERT INTO custom_field_template(id, field_id, template_id, scene, required, `order`, default_value,
                                          custom_data, `key`)
        SELECT UUID(),
               custom_field.id,
               test_case_template.id,
               scene,
               required,
               `order`,
               defaultValue,
               customData,
               `key`
        FROM custom_field,
             test_case_template
        WHERE custom_field.id LIKE CONCAT(fieldId, '%')
          AND test_case_template.id LIKE CONCAT(templateId, '%')
          AND (custom_field.project_id = test_case_template.project_id OR custom_field.project_id = 'global' OR
               test_case_template.project_id = 'global');

        INSERT INTO custom_field_template(id, field_id, template_id, scene, required, `order`, default_value,
                                          custom_data, `key`)
        SELECT UUID(),
               custom_field.id,
               issue_template.id,
               scene,
               required,
               `order`,
               defaultValue,
               customData,
               `key`
        FROM custom_field,
             issue_template
        WHERE custom_field.id LIKE CONCAT(fieldId, '%')
          AND issue_template.id LIKE CONCAT(templateId, '%')
          AND (custom_field.project_id = issue_template.project_id OR custom_field.project_id = 'global' OR
               issue_template.project_id = 'global');

    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL custom_field_template_ws();
DROP PROCEDURE IF EXISTS custom_field_template_ws;
DROP TABLE IF EXISTS custom_field_template_bak;


-- 去掉组织id
ALTER TABLE test_case_template
    DROP COLUMN workspace_id;

ALTER TABLE issue_template
    DROP COLUMN workspace_id;

ALTER TABLE custom_field
    DROP COLUMN workspace_id;

-- project 数据调整
UPDATE project
    JOIN test_case_template ON project.id = test_case_template.project_id
SET project.case_template_id = test_case_template.id
WHERE case_template_id IS NOT NULL
  AND test_case_template.id LIKE CONCAT(case_template_id, '%');

UPDATE project
    JOIN issue_template ON project.id = issue_template.project_id
SET project.issue_template_id = issue_template.id
WHERE issue_template_id IS NOT NULL
  AND issue_template.id LIKE CONCAT(issue_template_id, '%');


UPDATE project JOIN test_case_template ON project.id = test_case_template.project_id
SET case_template_id = test_case_template.id
WHERE case_template_id IS NULL AND test_case_template.`system` = 1;

UPDATE project JOIN issue_template ON project.id = issue_template.project_id
SET issue_template_id = issue_template.id
WHERE issue_template_id IS NULL AND issue_template.`system` = 1;

-- 默认设置权限
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TEMPLATE:READ+CASE_TEMPLATE', 'PROJECT_TEMPLATE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TEMPLATE:READ', 'PROJECT_TEMPLATE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TEMPLATE:READ+CUSTOM', 'PROJECT_TEMPLATE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TEMPLATE:READ+ISSUE_TEMPLATE', 'PROJECT_TEMPLATE');



-- project config
insert into project_application(project_id, type, type_value)
select id, 'CASE_CUSTOM_NUM', if(custom_num, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'CASE_CUSTOM_NUM');

insert into project_application(project_id, type, type_value)
select id, 'SCENARIO_CUSTOM_NUM', if(scenario_custom_num, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'SCENARIO_CUSTOM_NUM');

insert into project_application(project_id, type, type_value)
select id, 'API_QUICK_MENU', api_quick
from project
where api_quick is not null
  and not exists(select * from project_application where project_id = id and type = 'API_QUICK_MENU');


insert into project_application(project_id, type, type_value)
select id, 'CASE_PUBLIC', if(case_public, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'CASE_PUBLIC');


insert into project_application(project_id, type, type_value)
select id, 'MOCK_TCP_PORT', mock_tcp_port
from project
where project.mock_tcp_port is not null
    and not exists(select * from project_application where project_id = id and type = 'MOCK_TCP_PORT');


insert into project_application(project_id, type, type_value)
select id, 'MOCK_TCP_OPEN', if(is_mock_tcp_open, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'MOCK_TCP_OPEN');


insert into project_application(project_id, type, type_value)
select id, 'CLEAN_TRACK_REPORT', if(clean_api_report, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'CLEAN_TRACK_REPORT');


insert into project_application(project_id, type, type_value)
select id, 'CLEAN_TRACK_REPORT_EXPR', clean_track_report_expr
from project
where clean_track_report_expr is not null
  and not exists(select * from project_application where project_id = id and type = 'CLEAN_TRACK_REPORT_EXPR');

insert into project_application(project_id, type, type_value)
select id, 'CLEAN_API_REPORT', if(clean_api_report, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'CLEAN_API_REPORT');


insert into project_application(project_id, type, type_value)
select id, 'CLEAN_API_REPORT_EXPR', clean_api_report_expr
from project
where clean_api_report_expr is not null
  and not exists(select * from project_application where project_id = id and type = 'CLEAN_API_REPORT_EXPR');

insert into project_application(project_id, type, type_value)
select id, 'CLEAN_LOAD_REPORT', if(clean_load_report, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'CLEAN_LOAD_REPORT');

insert into project_application(project_id, type, type_value)
select id, 'CLEAN_LOAD_REPORT_EXPR', clean_load_report_expr
from project
where clean_load_report_expr is not null
  and not exists(select * from project_application where project_id = id and type = 'CLEAN_LOAD_REPORT_EXPR');

insert into project_application(project_id, type, type_value)
select id, 'URL_REPEATABLE', if(repeatable, 'true', 'false')
from project
where not exists(select * from project_application where project_id = id and type = 'URL_REPEATABLE');

-- drop column
alter table project drop column repeatable;

alter table project drop column custom_num;

alter table project drop column scenario_custom_num;

alter table project drop column mock_tcp_port;

alter table project drop column is_mock_tcp_open;

alter table project drop column api_quick;

alter table project drop column case_public;

alter table project drop column clean_track_report;

alter table project drop column clean_track_report_expr;

alter table project drop column clean_api_report;

alter table project drop column clean_api_report_expr;

alter table project drop column clean_load_report;

alter table project drop column clean_load_report_expr;



alter table api_test_environment
    add create_time bigint(13) null;

alter table api_test_environment
    add update_time bigint(13) null;

update api_test_environment set create_time = unix_timestamp() * 1000 where create_time is null;
update api_test_environment set update_time = unix_timestamp() * 1000 where update_time is null;

-- 删除历史脏数据
delete
from user_group_permission
where group_id = 'admin'
  and module_id = 'PROJECT_ERROR_REPORT_LIBRARY';

ALTER TABLE api_definition_exec_result
    ADD report_type varchar(100) DEFAULT 'API_INDEPENDENT' NOT NULL COMMENT '报告类型';

insert into api_definition_exec_result(id, name, resource_id, create_time, status, user_id, trigger_mode, start_time,
                                       end_time, actuator, report_type, version_id, project_id)
select id,
       name,
       id,
       create_time,
       status,
       user_id,
       trigger_mode,
       create_time,
       IF(end_time is null, update_time, end_time),
       actuator,
       report_type,
       version_id,
       project_id
from api_scenario_report
where execute_type = 'Saved'
  and report_type = 'API_INTEGRATED'
  and end_time is not null;


delete
from api_scenario_report
where report_type = 'API_INTEGRATED';

ALTER TABLE api_definition MODIFY COLUMN path varchar (1000);

#
DELETE FROM user_group_permission
WHERE group_id = 'project_app_manager';


ALTER TABLE `test_plan_report_content` ADD COLUMN `un_execute_cases` LONGTEXT COMMENT '未执行状态接口用例';
ALTER TABLE `test_plan_report_content` ADD COLUMN `un_execute_scenarios` LONGTEXT COMMENT '未执行状态场景用例';
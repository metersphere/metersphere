-- schedule
alter table schedule
    add config VARCHAR(500) null;


--  自动清理调试用的body文件
-- INSERT INTO schedule (id,`key`,`type`,value,`group`,job,enable,resource_id,user_id,workspace_id,create_time,update_time,project_id,name)
-- VALUES ('f7f99f50-850b-41b2-8cb9-edee70669c4b','f7f99f50-850b-41b2-8cb9-edee70669c4b','CRON','0 0 4 * * ?','CLEAR_JOB','io.metersphere.job.sechedule.ClearJob',1,'system','admin','system',unix_timestamp() * 1000,unix_timestamp() * 1000,'system','ClearJob');

CREATE TABLE `operating_log`
(
    `id`           varchar(50) NOT NULL COMMENT 'ID',
    `project_id`   varchar(50) NOT NULL COMMENT 'Project ID',
    `oper_method`  varchar(500)  DEFAULT NULL COMMENT 'operating method',
    `create_user`  varchar(100)  DEFAULT NULL COMMENT 'source create u',
    `oper_user`    varchar(50)   DEFAULT NULL COMMENT 'operating user id',
    `source_id`    varchar(6000) DEFAULT NULL COMMENT 'operating source id',
    `oper_type`    varchar(100)  DEFAULT NULL COMMENT 'operating type',
    `oper_module`  varchar(100)   DEFAULT NULL COMMENT 'operating module',
    `oper_title`   varchar(6000) DEFAULT NULL COMMENT 'operating title',
    `oper_path`    varchar(500)  DEFAULT NULL COMMENT 'operating path',
    `oper_content` longtext COMMENT 'operating content',
    `oper_params`  longtext COMMENT 'operating params',
    `oper_time`    bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- system_parameter
UPDATE system_parameter
SET param_key = 'base.prometheus.host'
WHERE param_key = 'prometheus.host';

-- load_test_report
alter table load_test_report
    add test_start_time BIGINT(13) null;

alter table load_test_report
    add test_end_time BIGINT(13) null;

alter table load_test_report
    add test_duration BIGINT(13) null;


-- add all table create_user
ALTER TABLE api_definition  ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_module  ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_scenario  ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_scenario_module  ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_scenario_report  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case_node  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case_review  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_plan_test_case  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case_review_test_case  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_plan_api_case  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_plan_api_scenario  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_plan_load_case  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case_report  ADD create_user VARCHAR(100) NULL;
ALTER TABLE project ADD create_user VARCHAR(100) NULL;
ALTER TABLE load_test  ADD create_user VARCHAR(100) NULL;
ALTER TABLE user  ADD create_user VARCHAR(100) NULL;
ALTER TABLE organization  ADD create_user VARCHAR(100) NULL;
ALTER TABLE workspace  ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_resource_pool  ADD create_user VARCHAR(100) NULL;
ALTER TABLE custom_field
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case_template
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE issue_template
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE test_case_report_template
    ADD create_user VARCHAR(100) NULL;
ALTER TABLE api_test_environment
    ADD create_user VARCHAR(100) NULL;


-- 缺陷来源
ALTER TABLE issues
    ADD resource_id varchar(50) DEFAULT NULL COMMENT 'Issue resource id';

alter table issues
    add num int null;

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


-- 修改 test_case type字段为为非必填
ALTER TABLE test_case
    MODIFY COLUMN `type` varchar(25) NULL COMMENT 'Test case type';

-- 表头修改（测试计划，测试用例添加创建人）
update system_header
set props='[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"createUser","label":"创建人"},{"id":"priority","label":"用例等级"},{"id":"reviewStatus","label":"评审状态"},{"id":"tags","label":"标签"},{"id":"nodePath","label":"所属模块"},{"id":"updateTime","label":"更新时间"}]'
where type = 'test_plan_list';


update system_header
set props='[{"id":"name","label":"名称"},{"id":"userName","label":"负责人"},{"id":"createUser","label":"创建人"},{"id":"status","label":"当前状态"},{"id":"stage","label":"测试阶段"},{"id":"testRate","label":"测试进度"},{"id":"projectName","label":"所属项目"},{"id":"plannedStartTime","label":"计划开始"},{"id":"plannedEndTime","label":"计划结束"},{"id":"actualStartTime","label":"实际开始"},{"id":"actualEndTime","label":"实际结束"},{"id":"tags","label":"标签"},{"id":"executionTimes","label":"执行次数"},{"id":"passRate","label":"通过率"}]';


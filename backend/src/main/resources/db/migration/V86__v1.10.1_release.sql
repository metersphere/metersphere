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

-- 清空system_header表
truncate table system_header;

insert into system_header (type, props)
values ('api_scenario_list',
        '[{"id":"num","label":"ID"},{"id":"name","label":"场景名称"},{"id":"level","label":"用例等级"},{"id":"status","label":"当前状态"},{"id":"tags","label":"标签"},{"id":"principal","label":"负责人"},{"id":"updateTime","label":"最后更新时间"},{"id":"stepTotal","label":"步骤数"},{"id":"lastResult","label":"最后结果"},{"id":"passRate","label":"通过率"}]');

insert into system_header (type, props)
values ('test_plan_function_test_case',
        '[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"priority","label":"用例等级"},{"id":"type","label":"类型"},{"id":"tags","label":"标签"},{"id":"nodePath","label":"所属模块"},{"id":"projectName","label":"所属项目"},{"id":"issuesContent","label":"缺陷"},{"id":"executorName","label":"执行人"},{"id":"status","label":"执行结果"},{"id":"updateTime","label":"更新时间"},{"id":"maintainer","label":"责任人"}]');
insert into system_header (type, props)
values ('test_case_review_case_list',
        '[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"priority","label":"用例等级"},{"id":"type","label":"类型"},{"id":"method","label":"测试方式"},{"id":"nodePath","label":"所属模块"},{"id":"projectName","label":"所属项目"},{"id":"reviewerName","label":"评审人"},{"id":"reviewStatus","label":"评审状态"},{"id":"updateTime","label":"更新时间"}]');

insert into system_header (type, props)
values ('test_plan_load_case',
        '[{"id":"num","label":"ID"},{"id":"caseName","label":"名称"},{"id":"projectName","label":"所属项目"},{"id":"userName","label":"创建人"},{"id":"createTime","label":"创建时间"},{"id":"status","label":"状态"},{"id":"caseStatus","label":"执行状态"},{"id":"loadReportId","label":"查看报告"}]');

insert into system_header (type, props)
values ('api_case_list',
        '[{"id":"num","label":"ID"},{"id":"name","label":"用例名称"},{"id":"priority","label":"用例等级"},{"id":"path","label":"路径"},{"id":"tags","label":"标签"},{"id":"createUser","label":"创建人"},{"id":"updateTime","label":"最后更新时间"}]');

insert into system_header (type, props)
values ('api_list',
        '[{"id":"num","label":"ID"},{"id":"status","label":"接口状态"},{"id":"name","label":"接口名称"},{"id":"method","label":"请求类型"},{"id":"userName","label":"负责人"},{"id":"path","label":"路径"},{"id":"tags","label":"标签"},{"id":"updateTime","label":"最后更新时间"},{"id":"caseTotal","label":"用例数"},{"id":"caseStatus","label":"用例状态"},{"id":"casePassingRate","label":"用例通过率"}]');

insert into system_header (type, props)
values ('test_case_review_list',
        '[{"id":"name","label":"评审名称"},{"id":"reviewer","label":"评审人"},{"id":"projectName","label":"所属项目"},{"id":"creatorName","label":"发起人"},{"id":"status","label":"当前状态"},{"id":"createTime","label":"创建时间"},{"id":"endTime","label":"截止时间"},{"id":"tags","label":"标签"}]');

insert into system_header (type, props)
values ('test_plan_api_case',
        '[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"priority","label":"用例等级"},{"id":"path","label":"路径"},{"id":"createUser","label":"创建人"},{"id":"custom","label":"最后更新时间"},{"id":"tags","label":"标签"},{"id":"execResult","label":"执行状态"}]');

insert into system_header (type, props)
values ('test_plan_list', '[{"id":"name","label":"名称"},{"id":"userName","label":"负责人"},{"id":"createUser","label":"创建人"},{"id":"status","label":"当前状态"},{"id":"stage","label":"测试阶段"},{"id":"testRate","label":"测试进度"},{"id":"projectName","label":"所属项目"},{"id":"plannedStartTime","label":"计划开始"},
{"id":"plannedEndTime","label":"计划结束"},{"id":"actualStartTime","label":"实际开始"},
{"id":"actualEndTime","label":"实际结束"},{"id":"tags","label":"标签"},
{"id":"executionTimes","label":"执行次数"},{"id":"passRate","label":"通过率"}]');

insert into system_header (type, props)
values ('test_case_list',
        '[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"createUser","label":"创建人"},{"id":"priority","label":"用例等级"},{"id":"reviewStatus","label":"评审状态"},{"id":"tags","label":"标签"},{"id":"nodePath","label":"所属模块"},{"id":"updateTime","label":"更新时间"}]');

insert into system_header (type, props)
values ('test_plan_scenario_case',
        '[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"level","label":"用例等级"},{"id":"tagNames","label":"标签"},{"id":"userId","label":"创建人"},{"id":"updateTime","label":"最后更新时间"},{"id":"stepTotal","label":"通过"},{"id":"lastResult","label":"失败"},{"id":"passRate","label":"通过率"}]');



-- 初始化权限
-- project_admin
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_PLAN:READ+RUN', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_ISSUE:READ', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_ISSUE:READ+CREATE', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_ISSUE:READ+EDIT', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_ISSUE:READ+DELETE', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_REPORT:READ', 'PROJECT_TRACK_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_REPORT:READ+DELETE', 'PROJECT_TRACK_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_TRACK_REPORT:READ+EXPORT', 'PROJECT_TRACK_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_API_REPORT:READ+EXPORT', 'PROJECT_API_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_PERFORMANCE_REPORT:READ+EXPORT', 'PROJECT_PERFORMANCE_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_admin', 'PROJECT_API_DEFINITION:READ+COPY_API', 'PROJECT_API_DEFINITION');

-- project_member
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_PLAN:READ+RUN', 'PROJECT_TRACK_PLAN');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_ISSUE:READ', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_ISSUE:READ+CREATE', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_ISSUE:READ+EDIT', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_ISSUE:READ+DELETE', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_REPORT:READ', 'PROJECT_TRACK_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_REPORT:READ+DELETE', 'PROJECT_TRACK_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_TRACK_REPORT:READ+EXPORT', 'PROJECT_TRACK_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_API_REPORT:READ+EXPORT', 'PROJECT_API_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_PERFORMANCE_REPORT:READ+EXPORT', 'PROJECT_PERFORMANCE_REPORT');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'project_member', 'PROJECT_API_DEFINITION:READ+COPY_API', 'PROJECT_API_DEFINITION');

-- read_only
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'read_only', 'PROJECT_TRACK_ISSUE:READ', 'PROJECT_TRACK_ISSUE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (uuid(), 'read_only', 'PROJECT_TRACK_REPORT:READ', 'PROJECT_TRACK_REPORT');

delete from user_group_permission where permission_id = 'PROJECT_TRACK_PLAN:READ+REPORT_DELETE';

-- 接口定义添加用例总数，用例状态，用例通过率字段
alter table api_definition
    add case_total varchar(100) null;
alter table api_definition
    add case_status varchar(100) null;
alter table api_definition
    add case_passing_rate varchar(100) null;

-- 测试用例表头添加责任人

update system_header
set props='[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"priority","label":"用例等级"},{"id":"type","label":"类型"},{"id":"method","label":"测试方式"},{"id":"nodePath","label":"所属模块"},{"id":"projectName","label":"所属项目"},{"id":"reviewerName","label":"评审人"},{"id":"maintainer","label":"责任人"},{"id":"reviewStatus","label":"评审状态"},{"id":"updateTime","label":"更新时间"}]'
where type = 'test_case_review_case_list';
update system_header
set props='[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"priority","label":"用例等级"},{"id":"path","label":"路径"},{"id":"createUser","label":"创建人"},{"id":"maintainer","label":"责任人"},{"id":"custom","label":"最后更新时间"},{"id":"tags","label":"标签"},{"id":"execResult","label":"执行状态"}]'
where type = 'test_plan_api_case';
update system_header
set props='[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"level","label":"用例等级"},{"id":"tagNames","label":"标签"},{"id":"userId","label":"创建人"},{"id":"maintainer","label":"责任人"},{"id":"updateTime","label":"最后更新时间"},{"id":"stepTotal","label":"通过"},{"id":"lastResult","label":"失败"},{"id":"passRate","label":"通过率"}]'
where type = 'test_plan_scenario_case';
update system_header
set props='[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"createUser","label":"创建人"},{"id":"maintainer","label":"责任人"},{"id":"priority","label":"用例等级"},{"id":"reviewStatus","label":"评审状态"},{"id":"tags","label":"标签"},{"id":"nodePath","label":"所属模块"},{"id":"updateTime","label":"更新时间"}]'
where type = 'test_case_list';

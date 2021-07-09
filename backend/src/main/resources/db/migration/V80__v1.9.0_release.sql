-- issues template and test case template

-- 自定义字段表
CREATE TABLE IF NOT EXISTS `custom_field` (
    `id`            varchar(50)  NOT NULL COMMENT 'Custom field ID',
    `name`          varchar(64)  NOT NULL COMMENT 'Custom field name',
    `scene`         varchar(30)  NOT NULL COMMENT 'Custom field use scene',
    `type`          varchar(30)  NOT NULL COMMENT 'Custom field type',
    `remark`        varchar(255) DEFAULT NULL COMMENT 'Custom field remark',
    `options`       text         DEFAULT NULL COMMENT 'Test resource pool status',
    `system`        tinyint(1)   DEFAULT 0 COMMENT 'Is system custom field',
    `global`        tinyint(1)   DEFAULT 0 COMMENT 'Is global custom field',
    `workspace_id`  varchar(50)  DEFAULT NULL COMMENT 'Workspace ID this custom field belongs to',
    `create_time`   bigint(13)   NOT NULL COMMENT 'Create timestamp',
    `update_time`   bigint(13)   NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- 用例系统字段
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('45f2de57-9d1d-11eb-b418-0242ac120002','用例状态','TEST_CASE','select','',
        '[{"value":"Prepare", "text":"test_track.case.status_prepare", "system": true},{"value":"Underway", "text":"test_track.case.status_running","system": true},{"value":"Completed", "text":"test_track.case.status_finished", "system": true}]',
        1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('46065143-9d1d-11eb-b418-0242ac120002','责任人','TEST_CASE','member','',
        '[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('4619cc23-9d1d-11eb-b418-0242ac120002','用例等级','TEST_CASE','select','',
        '[{"value":"P0", "text":"P0", "system": true},{"value":"P1", "text":"P1","system": true},{"value":"P2", "text":"P2", "system": true},{"value":"P3", "text":"P3", "system": true}]',
        1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

-- 缺陷系统字段
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('09642424-7b1b-4004-867e-ff9c798a1933','创建人','ISSUE','member','','[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('a577bc60-75fe-47ec-8aa6-32dca23bf3d6','处理人','ISSUE','member','','[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('beb57501-19c8-4ca3-8dfb-2cef7c0ea087','状态','ISSUE','select','','[{"text":"test_track.issue.status_new","value":"new","system": true},{"text":"test_track.issue.status_resolved","value":"resolved","system": true},{"text":"test_track.issue.status_closed","value":"closed","system": true}]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('d392af07-fdfe-4475-a459-87d59f0b1626','严重程度','ISSUE','select','','[{"text":"P0","value":"P0","system": true},{"text":"P1","value":"P1","system": true},{"text":"P2","value":"P2","system": true},{"text":"P3","value":"P3","system": true}]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

-- 用例模板表
CREATE TABLE IF NOT EXISTS `test_case_template` (
    `id`                varchar(50) NOT NULL COMMENT 'Field template ID',
    `name`              varchar(64) NOT NULL COMMENT 'Field template name',
    `type`              varchar(30) NOT NULL COMMENT 'Field template type',
    `description`       varchar(255) DEFAULT NULL COMMENT 'Field template description',
    `case_name`         varchar(64) NOT NULL COMMENT 'Test Case Name',
    `system`            tinyint(1)   DEFAULT 0 COMMENT 'Is system field template ',
    `global`            tinyint(1)   DEFAULT 0 COMMENT 'Is global template',
    `workspace_id`      varchar(50) DEFAULT NULL COMMENT 'Workspace ID this field template belongs to',
    `prerequisite`      varchar(255) DEFAULT NULL COMMENT 'Test case prerequisite condition',
    `step_description`  text DEFAULT NULL COMMENT 'Test case steps desc',
    `expected_result`   text DEFAULT NULL COMMENT 'Test case expected result',
    `actual_result`     text DEFAULT NULL COMMENT 'Test case actual result',
    `create_time`       bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`       bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- 系统模板
INSERT INTO test_case_template (id,name,`type`,description,case_name,prerequisite,step_description,expected_result,actual_result,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('b395d8fe-2ad6-4de7-81d3-2006b53a97c8','default','functional','Test case default template.','','','','','',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

-- 缺陷模板表
CREATE TABLE IF NOT EXISTS `issue_template` (
    `id`                varchar(50) NOT NULL COMMENT 'Field template ID',
    `name`              varchar(64) NOT NULL COMMENT 'Field template name',
    `platform`          varchar(30) NOT NULL COMMENT 'Field template type',
    `description`       varchar(255) DEFAULT NULL COMMENT 'Field template description',
    `title`             varchar(64) NOT NULL COMMENT 'Issue title',
    `system`            tinyint(1)   DEFAULT 0 COMMENT 'Is system field template ',
    `global`            tinyint(1)   DEFAULT 0 COMMENT 'Is global template',
    `workspace_id`      varchar(50) DEFAULT NULL COMMENT 'Workspace ID this field template belongs to',
    `content`           text DEFAULT NULL COMMENT 'Issue content',
    `create_time`       bigint(13)  NOT NULL COMMENT 'Create timestamp',
    `update_time`       bigint(13)  NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- 系统模板
INSERT INTO issue_template (id,`name`,platform,description,title,`global`,`system`,workspace_id,content,create_time,update_time)
VALUES ('5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','default','metersphere','','',1,1,'global','',unix_timestamp() * 1000,unix_timestamp() * 1000);

insert into issue_template (id,name,platform,description,title,`system`,`global`,workspace_id,content,create_time,update_time)
values ('c7f26296-cf62-4149-a4d2-ce2492729e41','JIRA-默认模版','Jira','JIRA默认模版','',1,1,'global','',unix_timestamp() * 1000,unix_timestamp() * 1000);

insert into issue_template (id,name,platform,description,title,`system`,`global`,workspace_id,content,create_time,update_time)
values ('f2b70824-471e-426e-9219-f82aba6dd560','禅道-默认模版','Zentao','禅道默认模版','',1,1,'global','', unix_timestamp() * 1000,unix_timestamp() * 1000);

insert into issue_template (id,name,platform,description,title,`system`,`global`,workspace_id,content,create_time,update_time)
values ('f2cd9e48-f136-4528-8249-a649c15aa3a4','TAPD-默认模版','Tapd','TAPD默认模版','',1,1,'global','',unix_timestamp() * 1000,unix_timestamp() * 1000);


-- 字段模板关联表
CREATE TABLE IF NOT EXISTS `custom_field_template` (
    `id`                varchar(50) NOT NULL COMMENT 'Custom field field template related id',
    `field_id`          varchar(50) NOT NULL COMMENT 'Custom field ID',
    `template_id`       varchar(50) NOT NULL COMMENT 'Field template ID',
    `scene`             varchar(30) NOT NULL COMMENT 'Use scene',
    `required`          tinyint(1) DEFAULT NULL COMMENT 'Is required',
    `order`             int(11)  DEFAULT NULL COMMENT 'Item order',
    `default_value`     varchar(30) DEFAULT NULL COMMENT 'Default value',
    `custom_data`       varchar(255) DEFAULT NULL COMMENT 'Custom data',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- 系统用例模板关联系统字段
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('0d2b90ec-9c56-4f5d-ae06-522a93f48e93','46065143-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','TEST_CASE',1,'');
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('661e1e29-c401-43b0-972e-a713b7b90c37','4619cc23-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','TEST_CASE',1,'"P0"');
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('f448e8d2-fad1-4c23-a013-3f3ba282bb81','45f2de57-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','TEST_CASE',1,'"Prepare"');

-- 系统缺陷模板关联系统字段
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('3d6a182f-cea7-452e-828d-4550edada409','d392af07-fdfe-4475-a459-87d59f0b1626','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','ISSUE',1,'"P0"');
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('657e70c3-0d1b-4bbe-b52f-bfadee05148a','09642424-7b1b-4004-867e-ff9c798a1933','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','ISSUE',1,'');
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('b8921cbc-05b3-4d8f-a96e-d1e4ae9d8664','a577bc60-75fe-47ec-8aa6-32dca23bf3d6','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','ISSUE',1,'');
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('d5908553-1b29-4868-9001-e938822b92ef','beb57501-19c8-4ca3-8dfb-2cef7c0ea087','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','ISSUE',1,'"new"');

ALTER TABLE project
    ADD case_template_id varchar(50) NULL COMMENT 'Relate test case template id';
ALTER TABLE project
    ADD issue_template_id varchar(50) NULL COMMENT 'Relate test issue template id';

-- add test_case
alter table test_case
    add step_description text null;
alter table test_case
    add expected_result text null;

-- api_scenario_report modify column length
ALTER TABLE api_scenario_report
    MODIFY COLUMN name VARCHAR(3000);
-- api_scenario_report modify column length
ALTER TABLE api_scenario_report MODIFY COLUMN scenario_id VARCHAR(3000);

-- 自定义字段需要修改的用例字段
ALTER TABLE test_case MODIFY COLUMN maintainer varchar(50) NULL COMMENT 'Test case maintainer';
ALTER TABLE test_case MODIFY COLUMN priority varchar(50) NULL COMMENT 'Test case priority';
ALTER TABLE test_case ADD custom_fields TEXT NULL COMMENT 'CustomField';
ALTER TABLE test_plan_test_case ADD actual_result TEXT NULL;


ALTER TABLE api_scenario_report
    MODIFY COLUMN scenario_id VARCHAR(3000);

-- create mock config
CREATE TABLE IF NOT EXISTS `mock_config`
(
    id             varchar(50)   not null,
    project_id     varchar(50)   not null,
    api_id         varchar(50)   not null,
    api_path       varchar(1000) null,
    api_method     varchar(64)   null,
    create_time    bigint(13)    null,
    update_time    bigint(13)    null,
    create_user_id VARCHAR(64)   null,
    primary key (id),
    INDEX `api_id` (`api_id`) USING BTREE,
    INDEX `project_id` (`project_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `mock_expect_config`
(
    id             varchar(50)   not null,
    mock_config_id varchar(50)   null,
    `name`         varchar(255)  null,
    `tags`         varchar(1000) null,
    request        longtext      null,
    response       longtext      null,
    STATUS         VARCHAR(10)   null,
    create_time    bigint(13)    null,
    update_time    bigint(13)    null,
    create_user_id VARCHAR(64)   null,
    primary key (id),
    INDEX `mock_config_id` (`mock_config_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- module management
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('metersphere.module.api', 'ENABLE', 'text', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('metersphere.module.performance', 'ENABLE', 'text', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('metersphere.module.reportStat', 'ENABLE', 'text', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('metersphere.module.testTrack', 'ENABLE', 'text', 1);


-- issue表添加自定义字段和项目id列
ALTER TABLE issues ADD custom_fields TEXT NULL COMMENT 'CustomField';
ALTER TABLE issues ADD project_id varchar(50) NULL;

-- 兼容旧数据，初始化issue表的project_id
update issues i
    inner join
    (
    select tc.project_id, tci.issues_id
    from test_case_issues tci
    inner join  test_case tc
    on tci.test_case_id = tc.id
    ) as tmp
on i.id = tmp.issues_id
    set i.project_id = tmp.project_id;

-- 修改issue表主键
alter table issues drop primary key;
alter table issues
    add constraint issues_pk
        primary key (id);

-- init prometheus host
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('prometheus.host', 'http://ms-prometheus:9090', 'text', 1);

-- 报告新增的字段
alter table load_test_report
    add max_users VARCHAR(10) null;

alter table load_test_report
    add avg_response_time VARCHAR(10) null;

alter table load_test_report
    add tps VARCHAR(10) null;

-- 模板的字段不必填
ALTER TABLE issue_template MODIFY COLUMN title varchar(64) NULL COMMENT 'Issue title';
ALTER TABLE test_case_template MODIFY COLUMN case_name varchar(64) NULL COMMENT 'Test Case Name';

-- 用例步骤支持两种编辑模式
ALTER TABLE test_case_template ADD step_model varchar(10) NULL COMMENT 'Step model';
ALTER TABLE test_case_template ADD steps TEXT NULL COMMENT 'Test case step';
ALTER TABLE test_case ADD step_model varchar(10) NULL COMMENT 'Test case step model';

-- 去掉测试方式
UPDATE system_header
SET props='[{"id":"num","label":"ID"},{"id":"name","label":"名称"},{"id":"priority","label":"用例等级"},{"id":"type","label":"类型"},{"id":"tags","label":"标签"},{"id":"nodePath","label":"所属模块"},{"id":"projectName","label":"所属项目"},{"id":"issuesContent","label":"缺陷"},{"id":"executorName","label":"执行人"},{"id":"status","label":"执行结果"},{"id":"updateTime","label":"更新时间"},{"id":"maintainer","label":"责任人"}]'
WHERE `type`='test_plan_function_test_case';

-- 自定义用例ID
alter table project add custom_num tinyint(1) default 0 null comment '是否开启自定义ID(默认关闭)';
alter table test_case add custom_num varchar(64) null comment 'custom num';

-- 修改前置条件为text
ALTER TABLE test_case MODIFY COLUMN prerequisite text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Test case prerequisite condition';

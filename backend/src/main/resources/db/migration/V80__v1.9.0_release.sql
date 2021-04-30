-- issues template and test case template

-- 自定义字段表
create table if not exists `custom_field` (
    `id`            varchar(50)  not null comment 'custom field id',
    `name`          varchar(64)  not null comment 'custom field name',
    `scene`         varchar(30)  not null comment 'custom field use scene',
    `type`          varchar(30)  not null comment 'custom field type',
    `remark`        varchar(255) default null comment 'custom field remark',
    `options`       text         default null comment 'test resource pool status',
    `system`        tinyint(1)   default 0 comment 'is system custom field',
    `global`        tinyint(1)   default 0 comment 'is global custom field',
    `workspace_id`  varchar(50)  default null comment 'workspace id this custom field belongs to',
    `create_time`   bigint(13)   not null comment 'create timestamp',
    `update_time`   bigint(13)   not null comment 'update timestamp',
    primary key (`id`)
)
    engine = innodb
    default charset = utf8mb4;

-- 用例系统字段
insert into custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
values ('45f2de57-9d1d-11eb-b418-0242ac120002','用例状态','test_case','select','',
        '[{"value":"prepare", "text":"test_track.case.status_prepare", "system": true},{"value":"underway", "text":"test_track.case.status_running","system": true},{"value":"completed", "text":"test_track.case.status_finished", "system": true}]',
        1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
insert into custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
values ('46065143-9d1d-11eb-b418-0242ac120002','责任人','test_case','member','',
        '[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
insert into custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
values ('4619cc23-9d1d-11eb-b418-0242ac120002','用例等级','test_case','select','',
        '[{"value":"p0", "text":"p0", "system": true},{"value":"p1", "text":"p1","system": true},{"value":"p2", "text":"p2", "system": true},{"value":"p3", "text":"p3", "system": true}]',
        1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

-- 缺陷系统字段
insert into custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
values ('09642424-7b1b-4004-867e-ff9c798a1933','创建人','issue','member','','[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
insert into custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
values ('a577bc60-75fe-47ec-8aa6-32dca23bf3d6','处理人','issue','member','','[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
insert into custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
values ('beb57501-19c8-4ca3-8dfb-2cef7c0ea087','状态','issue','select','','[{"text":"test_track.issue.status_new","value":"new","system": true},{"text":"test_track.issue.status_resolved","value":"resolved","system": true},{"text":"test_track.issue.status_closed","value":"closed","system": true}]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
insert into custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
values ('d392af07-fdfe-4475-a459-87d59f0b1626','严重程度','issue','select','','[{"text":"p0","value":"p0","system": true},{"text":"p1","value":"p1","system": true},{"text":"p2","value":"p2","system": true},{"text":"p3","value":"p3","system": true}]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

-- 用例模板表
create table if not exists `test_case_template` (
    `id`                varchar(50) not null comment 'field template id',
    `name`              varchar(64) not null comment 'field template name',
    `type`              varchar(30) not null comment 'field template type',
    `description`       varchar(255) default null comment 'field template description',
    `case_name`         varchar(64) not null comment 'test case name',
    `system`            tinyint(1)   default 0 comment 'is system field template ',
    `global`            tinyint(1)   default 0 comment 'is global template',
    `workspace_id`      varchar(50) default null comment 'workspace id this field template belongs to',
    `prerequisite`      varchar(255) default null comment 'test case prerequisite condition',
    `step_description`  text default null comment 'test case steps desc',
    `expected_result`   text default null comment 'test case expected result',
    `actual_result`     text default null comment 'test case actual result',
    `create_time`       bigint(13)  not null comment 'create timestamp',
    `update_time`       bigint(13)  not null comment 'update timestamp',
    primary key (`id`)
)
    engine = innodb
    default charset = utf8mb4;

-- 系统模板
insert into test_case_template (id,name,`type`,description,case_name,prerequisite,step_description,expected_result,actual_result,`system`,`global`,workspace_id,create_time,update_time)
values ('b395d8fe-2ad6-4de7-81d3-2006b53a97c8','default','functional','test case default template.','','','','','',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

-- 缺陷模板表
create table if not exists `issue_template` (
    `id`                varchar(50) not null comment 'field template id',
    `name`              varchar(64) not null comment 'field template name',
    `platform`          varchar(30) not null comment 'field template type',
    `description`       varchar(255) default null comment 'field template description',
    `title`             varchar(64) not null comment 'issue title',
    `system`            tinyint(1)   default 0 comment 'is system field template ',
    `global`            tinyint(1)   default 0 comment 'is global template',
    `workspace_id`      varchar(50) default null comment 'workspace id this field template belongs to',
    `content`           text default null comment 'issue content',
    `create_time`       bigint(13)  not null comment 'create timestamp',
    `update_time`       bigint(13)  not null comment 'update timestamp',
    primary key (`id`)
)
    engine = innodb
    default charset = utf8mb4;

-- 系统模板
insert into issue_template (id,`name`,platform,description,title,`global`,`system`,workspace_id,content,create_time,update_time)
values ('5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','default','metersphere','','',1,1,'global','',unix_timestamp() * 1000,unix_timestamp() * 1000);

insert into issue_template (id,name,platform,description,title,`system`,`global`,workspace_id,content,create_time,update_time)
values ('c7f26296-cf62-4149-a4d2-ce2492729e41','jira-默认模版','jira','jira默认模版','',1,1,'global','',unix_timestamp() * 1000,unix_timestamp() * 1000);

insert into issue_template (id,name,platform,description,title,`system`,`global`,workspace_id,content,create_time,update_time)
values ('f2b70824-471e-426e-9219-f82aba6dd560','禅道-默认模版','zentao','禅道默认模版','',1,1,'global','', unix_timestamp() * 1000,unix_timestamp() * 1000);

insert into issue_template (id,name,platform,description,title,`system`,`global`,workspace_id,content,create_time,update_time)
values ('f2cd9e48-f136-4528-8249-a649c15aa3a4','tapd-默认模版','tapd','tapd默认模版','',1,1,'global','',unix_timestamp() * 1000,unix_timestamp() * 1000);


-- 字段模板关联表
create table if not exists `custom_field_template` (
    `id`                varchar(50) not null comment 'custom field field template related id',
    `field_id`          varchar(50) not null comment 'custom field id',
    `template_id`       varchar(50) not null comment 'field template id',
    `scene`             varchar(30) not null comment 'use scene',
    `required`          tinyint(1) default null comment 'is required',
    `order`             int(11)  default null comment 'item order',
    `default_value`     varchar(30) default null comment 'default value',
    `custom_data`       varchar(255) default null comment 'custom data',
    primary key (`id`)
)
    engine = innodb
    default charset = utf8mb4;

-- 系统用例模板关联系统字段
insert into custom_field_template (id,field_id,template_id,scene,required,default_value)
values ('0d2b90ec-9c56-4f5d-ae06-522a93f48e93','46065143-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','test_case',1,'');
insert into custom_field_template (id,field_id,template_id,scene,required,default_value)
values ('661e1e29-c401-43b0-972e-a713b7b90c37','4619cc23-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','test_case',1,'"p0"');
insert into custom_field_template (id,field_id,template_id,scene,required,default_value)
values ('f448e8d2-fad1-4c23-a013-3f3ba282bb81','45f2de57-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','test_case',1,'"prepare"');

-- 系统缺陷模板关联系统字段
insert into custom_field_template (id,field_id,template_id,scene,required,default_value)
values ('3d6a182f-cea7-452e-828d-4550edada409','d392af07-fdfe-4475-a459-87d59f0b1626','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','issue',1,'"p0"');
insert into custom_field_template (id,field_id,template_id,scene,required,default_value)
values ('657e70c3-0d1b-4bbe-b52f-bfadee05148a','09642424-7b1b-4004-867e-ff9c798a1933','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','issue',1,'');
insert into custom_field_template (id,field_id,template_id,scene,required,default_value)
values ('b8921cbc-05b3-4d8f-a96e-d1e4ae9d8664','a577bc60-75fe-47ec-8aa6-32dca23bf3d6','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','issue',1,'');
insert into custom_field_template (id,field_id,template_id,scene,required,default_value)
values ('d5908553-1b29-4868-9001-e938822b92ef','beb57501-19c8-4ca3-8dfb-2cef7c0ea087','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','issue',1,'"new"');

alter table project
    add case_template_id varchar(50) null comment 'relate test case template id';
alter table project
    add issue_template_id varchar(50) null comment 'relate test issue template id';

-- add test_case
alter table test_case
    add step_description text null;
alter table test_case
    add expected_result text null;

-- api_scenario_report modify column length
alter table api_scenario_report
    modify column name varchar(3000);
-- api_scenario_report modify column length
alter table api_scenario_report modify column scenario_id varchar(3000);

-- 自定义字段需要修改的用例字段
alter table test_case modify column maintainer varchar(50) null comment 'test case maintainer';
alter table test_case modify column priority varchar(50) null comment 'test case priority';
alter table test_case add custom_fields text null comment 'customfield';
alter table test_plan_test_case add actual_result text null;


alter table api_scenario_report
    modify column scenario_id varchar(3000);

-- create mock config
create table if not exists `mock_config`
(
    id             varchar(50)   not null,
    project_id     varchar(50)   not null,
    api_id         varchar(50)   not null,
    api_path       varchar(1000) null,
    api_method     varchar(64)   null,
    create_time    bigint(13)    null,
    update_time    bigint(13)    null,
    create_user_id varchar(64)   null,
    primary key (id),
    index `api_id` (`api_id`) using btree,
    index `project_id` (`project_id`) using btree
) engine = innodb
  default charset = utf8mb4;

create table if not exists `mock_expect_config`
(
    id             varchar(50)   not null,
    mock_config_id varchar(50)   null,
    `name`         varchar(255)  null,
    `tags`         varchar(1000) null,
    request        longtext      null,
    response       longtext      null,
    status         varchar(10)   null,
    create_time    bigint(13)    null,
    update_time    bigint(13)    null,
    create_user_id varchar(64)   null,
    primary key (id),
    index `mock_config_id` (`mock_config_id`) using btree
) engine = innodb
  default charset = utf8mb4;

-- module management
insert into system_parameter (param_key, param_value, type, sort)
values ('metersphere.module.api', 'enable', 'text', 1);
insert into system_parameter (param_key, param_value, type, sort)
values ('metersphere.module.performance', 'enable', 'text', 1);
insert into system_parameter (param_key, param_value, type, sort)
values ('metersphere.module.reportstat', 'enable', 'text', 1);
insert into system_parameter (param_key, param_value, type, sort)
values ('metersphere.module.testtrack', 'enable', 'text', 1);


-- issue表添加自定义字段和项目id列
alter table issues add custom_fields text null comment 'customfield';
alter table issues add project_id varchar(50) null;

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
insert into system_parameter (param_key, param_value, type, sort)
values ('prometheus.host', 'http://ms-prometheus:9090', 'text', 1);

-- 报告新增的字段
alter table load_test_report
    add max_users varchar(10) null;

alter table load_test_report
    add avg_response_time varchar(10) null;

alter table load_test_report
    add tps varchar(10) null;

-- 模板的字段不必填
alter table issue_template modify column title varchar(64) null comment 'issue title';
alter table test_case_template modify column case_name varchar(64) null comment 'test case name';

-- 用例步骤支持两种编辑模式
alter table test_case_template add step_model varchar(10) null comment 'step model';
alter table test_case_template add steps text null comment 'test case step';
alter table test_case add step_model varchar(10) null comment 'test case step model';

-- 去掉测试方式
update system_header
set props='[{"id":"num","label":"id"},{"id":"name","label":"名称"},{"id":"priority","label":"用例等级"},{"id":"type","label":"类型"},{"id":"tags","label":"标签"},{"id":"nodepath","label":"所属模块"},{"id":"projectname","label":"所属项目"},{"id":"issuescontent","label":"缺陷"},{"id":"executorname","label":"执行人"},{"id":"status","label":"执行结果"},{"id":"updatetime","label":"更新时间"},{"id":"maintainer","label":"责任人"}]'
where `type`='test_plan_function_test_case';

-- 自定义用例id
alter table project add custom_num tinyint(1) default 0 null comment '是否开启自定义id(默认关闭)';
alter table test_case add custom_num varchar(64) null comment 'custom num';

-- 修改前置条件为text
alter table test_case modify column prerequisite text character set utf8mb4 collate utf8mb4_general_ci null comment 'test case prerequisite condition';

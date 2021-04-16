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
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

-- 用例系统字段
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('45f2de57-9d1d-11eb-b418-0242ac120002','custom_field.case_status','TEST_CASE','select','',
        '[{"value":"Prepare", "text":"test_track.case.status_prepare", "system": true},{"value":"Underway", "text":"test_track.case.status_running","system": true},{"value":"Completed", "text":"test_track.case.status_finished", "system": true}]',
        1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('46065143-9d1d-11eb-b418-0242ac120002','custom_field.case_maintainer','TEST_CASE','member','',
        '[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('4619cc23-9d1d-11eb-b418-0242ac120002','custom_field.case_priority','TEST_CASE','select','',
        '[{"value":"PO", "text":"PO", "system": true},{"value":"P1", "text":"P1","system": true},{"value":"P2", "text":"P2", "system": true},{"value":"P3", "text":"P3", "system": true}]',
        1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

-- 缺陷系统字段
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('09642424-7b1b-4004-867e-ff9c798a1933','custom_field.issue_creator','ISSUE','member','','[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('a577bc60-75fe-47ec-8aa6-32dca23bf3d6','custom_field.issue_processor','ISSUE','member','','[]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('beb57501-19c8-4ca3-8dfb-2cef7c0ea087','custom_field.issue_status','ISSUE','select','','[{"text":"test_track.issue.status_new","value":"NEW","system": true},{"text":"test_track.issue.status_resolved","value":"RESOLVED","system": true},{"text":"test_track.issue.status_closed","value":"CLOSED","system": true}]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);
INSERT INTO custom_field (id,name,scene,`type`,remark,`options`,`system`,`global`,workspace_id,create_time,update_time)
VALUES ('d392af07-fdfe-4475-a459-87d59f0b1626','custom_field.issue_severity','ISSUE','select','','[{"text":"P0","value":"P0","system": true},{"text":"P1","value":"P1","system": true},{"text":"P2","value":"P2","system": true},{"text":"P3","value":"P3","system": true}]',1,1,'global',unix_timestamp() * 1000,unix_timestamp() * 1000);

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
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

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
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

-- 系统模板
INSERT INTO issue_template (id,`name`,platform,description,title,`global`,`system`,workspace_id,content,create_time,update_time)
VALUES ('5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','default','metersphere','','',1,1,'global','',unix_timestamp() * 1000,unix_timestamp() * 1000);

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
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

-- 系统用例模板关联系统字段
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('0d2b90ec-9c56-4f5d-ae06-522a93f48e93','46065143-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','TEST_CASE',1,'');
INSERT INTO custom_field_template (id,field_id,template_id,scene,required,default_value)
VALUES ('661e1e29-c401-43b0-972e-a713b7b90c37','4619cc23-9d1d-11eb-b418-0242ac120002','b395d8fe-2ad6-4de7-81d3-2006b53a97c8','TEST_CASE',1,'"PO"');
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
VALUES ('d5908553-1b29-4868-9001-e938822b92ef','beb57501-19c8-4ca3-8dfb-2cef7c0ea087','5d7c87d2-f405-4ec1-9a3d-71b514cdfda3','ISSUE',1,'"NEW"');

ALTER TABLE project ADD case_template_id varchar(50) NULL COMMENT 'Relate test case template id';
ALTER TABLE project ADD issue_template_id varchar(50) NULL COMMENT 'Relate test issue template id';

-- add test_case
alter table test_case
    add step_description text null;
alter table test_case
    add expected_result text null;

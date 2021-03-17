-- create tale user_header
CREATE TABLE IF NOT EXISTS `user_header`
(
    id      varchar(50)   not null,
    user_id varchar(50)   null,
    props   varchar(1000) null,
    type    varchar(150)  null,
    constraint user_header_pk
        primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- create table test_case_review_api_case
create table test_case_review_api_case
(
    id                  varchar(50) not null,
    test_case_review_id varchar(50) null,
    api_case_id         varchar(50) null,
    status              varchar(50) null,
    environment_id      varchar(50) null,
    create_time         bigint(13)  null,
    update_time         bigint(13)  null,
    primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- create table test_case_review_scenario
create table test_case_review_scenario
(
    id                  varchar(50)  not null,
    test_case_review_id varchar(50)  null,
    api_scenario_id     varchar(50)  null,
    status              varchar(50)  null,
    environment         varchar(50)  null,
    create_time         bigint(13)   null,
    update_time         bigint(13)   null,
    pass_rate           varchar(100) null,
    last_result         varchar(100) null,
    report_id           varchar(50)  null,
    primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- create table test_case_review_load
create table test_case_review_load
(
    id                  varchar(50) not null,
    test_case_review_id varchar(50) null,
    load_case_id        varchar(50) null,
    status              varchar(50) null,
    create_time         bigint(13)  null,
    update_time         bigint(13)  null,
    load_report_id      varchar(50) null,
    primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- test_resource_pool add column
ALTER TABLE test_resource_pool ADD heap VARCHAR(200) NULL;
ALTER TABLE test_resource_pool ADD gc_algo VARCHAR(200) NULL;

-- create tale api_document_share
CREATE TABLE IF NOT EXISTS `api_document_share`  (
     `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Api Document Share Info ID',
     `create_time` BIGINT ( 13 ) NOT NULL COMMENT 'Create timestamp',
     `create_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
     `update_time` BIGINT ( 13 ) NOT NULL COMMENT 'last visit timestamp',
     `share_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'single or batch',
     `share_api_id` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'APiDefinition.id (JSONArray format. Order by TreeSet)',
     PRIMARY KEY (`id`) USING BTREE,
     INDEX `share_type`(`share_type`) USING BTREE,
     INDEX `share_api_id`(`share_api_id`(125)) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- swagger_url_project
alter table swagger_url_project
    modify module_id varchar(120) null;

-- add_test_case
alter table test_case
    add demand_id varchar(120) null;

alter table test_case
    add demand_name varchar(999) null;
alter table test_case
    add follow_people varchar(100) null;
alter table test_case
    add status varchar(25) null;
-- test_case_review add column
ALTER TABLE test_case_review
    ADD tags VARCHAR(2000) NULL;

-- alter test_plan_api_scenario
alter table test_plan_api_scenario
    change environment_id environment longtext null comment 'Relevance environment';

-- add Original state
alter table api_definition add original_state varchar(64);
alter table api_scenario add original_state varchar(64);
update api_definition set original_state='Underway';
update api_scenario set original_state='Underway';

-- alter test_case_review_scenario
alter table test_case_review_scenario modify environment longtext null;


-- schedule table add project_id column
alter table schedule add project_id varchar(50) NULL;
-- set values for new colums of exitsting data
update schedule sch inner join test_plan testPlan on
      testPlan.id = sch.resource_id
      set sch.project_id = testPlan.project_id where
            sch.resource_id = testPlan.id;
update schedule sch inner join swagger_url_project sup on
      sup.id = sch.resource_id
      set sch.project_id = sup.project_id where
            sch.resource_id = sup.id;
update schedule sch inner join api_scenario apiScene on
      apiScene.id = sch.resource_id
      set sch.project_id = apiScene.project_id where
            sch.resource_id = apiScene.id;
update schedule sch inner join load_test ldt on
      ldt.id = sch.resource_id
      set sch.project_id = ldt.project_id where
            sch.resource_id = ldt.id;
update schedule sch inner join api_test apiTest on
      apiTest.id = sch.resource_id
      set sch.project_id = apiTest.project_id where
            sch.resource_id = apiTest.id;
-- schedule table add name column
alter table schedule add name varchar(100) NULL;
-- set values for new colums of exitsting data
update schedule sch inner join api_scenario apiScene on
	apiScene.id = sch.resource_id
	set sch.name = apiScene.name;
update schedule sch inner join test_plan testPlan on
	testPlan.id = sch.resource_id
	set sch.name = testPlan.name;
update schedule sch inner join load_test ldt on
	ldt.id = sch.resource_id
	set sch.name = ldt.name;
update schedule sch inner join api_test apiTest on
	apiTest.id = sch.resource_id
	set sch.name = apiTest.name;
update schedule sch inner join swagger_url_project sup on
	sup.id = sch.resource_id
	set sch.name = LEFT(SUBSTRING_INDEX(sup.swagger_url, '/', 3), 100);
-- delete an unused colum
alter table schedule drop column custom_data;

-- add sort column
alter table load_test_file add sort int default 0;

alter table file_metadata
    add project_id VARCHAR(50) null;

UPDATE file_metadata JOIN (SELECT file_id, project_id
                           FROM load_test_file
                                    JOIN load_test ON test_id = load_test.id) temp ON file_id = file_metadata.id
SET file_metadata.project_id = temp.project_id;

UPDATE file_metadata JOIN (SELECT file_id, project_id
                           FROM api_test_file
                                    JOIN api_test ON test_id = api_test.id) temp ON file_id = file_metadata.id
SET file_metadata.project_id = temp.project_id;
CREATE INDEX load_test_report_test_resource_pool_id_index
    ON load_test_report (test_resource_pool_id);



create table if not exists test_plan_principal
(
    test_plan_id varchar(50) null,
    principal_id varchar(50) null,
    constraint test_plan_principal_pk
        unique (test_plan_id, principal_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

insert into test_plan_principal(test_plan_id, principal_id) select id test_plan_id, principal principal_id from test_plan;
alter table test_plan modify principal varchar(50) null comment 'Plan principal';


ALTER TABLE test_case_review_test_case ADD `order` bigint(20) NOT NULL COMMENT '自定义排序，间隔5000';


-- 报表默认权限插入

-- 项目管理员
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_REPORT_ANALYSIS:READ+UPDATE', 'PROJECT_REPORT_ANALYSIS');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_REPORT_ANALYSIS:READ+CREATE', 'PROJECT_REPORT_ANALYSIS');

-- 项目成员
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_REPORT_ANALYSIS:READ+UPDATE', 'PROJECT_REPORT_ANALYSIS');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_REPORT_ANALYSIS:READ+CREATE', 'PROJECT_REPORT_ANALYSIS');

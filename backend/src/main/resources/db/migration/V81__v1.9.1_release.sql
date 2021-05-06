ALTER TABLE api_scenario_report MODIFY COLUMN scenario_name VARCHAR(3000);

alter table project add scenario_custom_num tinyint(1) default 0 null comment '是否开启场景自定义ID(默认关闭)';
alter table api_scenario add custom_num varchar(64) null comment 'custom num';
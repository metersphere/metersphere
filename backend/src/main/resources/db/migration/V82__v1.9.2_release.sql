-- 缺陷添加创建人字段
ALTER TABLE issues ADD creator varchar(50) NULL COMMENT 'Creator';

-- 删除创建人的自定义字段
delete cft,cf from custom_field_template cft,custom_field cf
    where cft.field_id = cf.id and cf.name = '创建人' and cf.scene = 'ISSUE';

alter table project add scenario_custom_num tinyint(1) default 0 null comment '是否开启场景自定义ID(默认关闭)';
alter table api_scenario add custom_num varchar(64) null comment 'custom num';
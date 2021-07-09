-- 缺陷添加创建人字段
ALTER TABLE issues
    ADD creator varchar(50) NULL COMMENT 'Creator';

-- 删除创建人的自定义字段
delete cft,cf
from custom_field_template cft,
     custom_field cf
where cft.field_id = cf.id
  and cf.name = '创建人'
  and cf.scene = 'ISSUE';

alter table project
    add scenario_custom_num tinyint(1) default 0 null comment '是否开启场景自定义ID(默认关闭)';
alter table api_scenario
    add custom_num varchar(64) null comment 'custom num';
-- 场景表头修改
update system_header
set props='[{"id":"num","label":"ID"},{"id":"name","label":"场景名称"},{"id":"level","label":"用例等级"},{"id":"status","label":"当前状态"},{"id":"tags","label":"标签"},{"id":"principal","label":"负责人"},{"id":"updateTime","label":"最后更新时间"},{"id":"stepTotal","label":"步骤数"},{"id":"lastResult","label":"最后结果"},{"id":"passRate","label":"通过率"}]'
where type = 'api_scenario_list';
update system_header
set props= '[{"id":"num","label":"ID"},{"id":"status","label":"接口状态"},{"id":"name","label":"接口名称"},{"id":"method","label":"请求类型"},{"id":"userName","label":"负责人"},{"id":"path","label":"路径"},{"id":"tags","label":"标签"},{"id":"updateTime","label":"最后更新时间"},{"id":"caseTotal","label":"用例数"},{"id":"caseStatus","label":"用例状态"},{"id":"casePassingRate","label":"用例通过率"}]'
where type = 'api_list';

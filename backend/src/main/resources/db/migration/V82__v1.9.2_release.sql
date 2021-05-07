-- 缺陷添加创建人字段
ALTER TABLE issues ADD creator varchar(50) NULL COMMENT 'Creator';

-- 删除创建人的自定义字段
delete cft,cf from custom_field_template cft,custom_field cf
    where cft.field_id = cf.id and cf.name = '创建人' and cf.scene = 'ISSUE';

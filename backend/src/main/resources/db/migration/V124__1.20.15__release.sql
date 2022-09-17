-- V124__1-20-15__custom_field_template
-- 工单名称 V124__1-20-15__custom_field_template
-- 创建人 wangxiaogang
-- 创建时间 2022-09-15 16:39:06
-- 工单描述 修改自定义字段模版表的默认值类型为text

ALTER TABLE custom_field_template MODIFY default_value TEXT NULL;
ALTER TABLE test_case_template MODIFY prerequisite TEXT NULL;

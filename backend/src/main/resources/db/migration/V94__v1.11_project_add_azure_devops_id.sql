alter table project add azure_devops_id varchar(50) null after zentao_id;
alter table custom_field_template modify default_value varchar(100) null ;
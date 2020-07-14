alter table user add source varchar(50) null;

update user set source = 'Local' where source is null;
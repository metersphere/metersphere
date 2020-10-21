alter table message_task
	add identification varchar(255) not null;
alter table message_task
    add result boolean not null;
alter table message_task change result is_Set tinyint(1) not null;
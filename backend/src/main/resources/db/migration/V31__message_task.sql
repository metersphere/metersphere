create table message_task
(
	id varchar(255) not null,
	type varchar(255) not null comment '消息类型',
	event varchar(255) not null comment '通知事件类型',
	userId varchar(500) not null comment '接收人id',
	userName varchar(500) not null comment '接收人姓名',
	taskType varchar(255) not null,
	webhook varchar(255) not null comment 'webhook地址',
	constraint message_manage_pk
		primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
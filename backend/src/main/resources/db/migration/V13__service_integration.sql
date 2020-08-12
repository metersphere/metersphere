create table service_integration
(
	id varchar(50) not null,
	organization_id varchar(50) not null,
	platform varchar(50) not null,
	configuration text not null,
	constraint service_integration_pk
		primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (uuid(), 'ws_admin', 'WORKSPACE_TEMPLATE:READ+API_TEMPLATE', 'WORKSPACE_TEMPLATE');

alter table project
    add api_template_id varchar(50) null comment 'Relate api template id';

CREATE TABLE IF NOT EXISTS `api_template`
(
    `id`           varchar(50)  NOT NULL COMMENT 'Field template ID',
    `name`         varchar(64)  NOT NULL COMMENT 'Field template name',
    type           varchar(30)  NOT NULL comment 'Field template type',
    `description`  varchar(255) DEFAULT NULL COMMENT 'Field template description',
    `system`       tinyint(1)   DEFAULT 0 COMMENT 'Is system field template ',
    `global`       tinyint(1)   DEFAULT 0 COMMENT 'Is global template',
    `workspace_id` varchar(50)  DEFAULT NULL COMMENT 'Workspace ID this field template belongs to',
    `create_user`  varchar(100) NULL,
    `api_name`     varchar(64)  NULL COMMENT 'Api Name',
    `api_method`   varchar(64)  NOT NULL COMMENT 'Api Method',
    `api_path`     varchar(1000)   NULL COMMENT 'Api Path',
    `create_time`  bigint(13)   NOT NULL COMMENT 'Create timestamp',
    `update_time`  bigint(13)   NOT NULL COMMENT 'Update timestamp',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE utf8mb4_general_ci;


INSERT INTO api_template (id, name, `type`, description,api_name, `system`, `global`, workspace_id, api_method,create_time,
                          update_time)
VALUES (uuid(), 'default', 'HTTP', 'API default template.','', 1, 1, 'global','GET', unix_timestamp() * 1000,
        unix_timestamp() * 1000);


alter table api_definition
	add custom_fields text null comment 'CustomField';

alter table swagger_url_project
	add custom_fields text null comment 'CustomField';
-- v2_2 init
-- v2_2_init_ui_share_report_time
-- 工单名称: v2_2_init_ui_share_report_time
-- 创建人: liuyao
-- 创建时间: 2022-09-05 12:35:54
-- 工单描述: 初始化UI报告分享链接失效时间
DROP PROCEDURE IF EXISTS project_ui_appl;

DELIMITER //
CREATE PROCEDURE project_ui_appl()
BEGIN
    #声明结束标识
    DECLARE end_flag INT DEFAULT 0;

    DECLARE projectId VARCHAR(64);

    #声明游标 group_curosr
    DECLARE project_curosr CURSOR FOR SELECT DISTINCT id FROM project;

#设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET end_flag = 1;

    #打开游标
    OPEN project_curosr;
    #获取当前游标指针记录，取出值赋给自定义的变量
    FETCH project_curosr INTO projectId;
    #遍历游标
    REPEAT
        #利用取到的值进行数据库的操作
        DELETE FROM project_application WHERE project_id = projectId AND TYPE = 'UI_SHARE_REPORT_TIME';
        INSERT INTO project_application (project_id, TYPE, type_value)
        VALUES (projectId, 'UI_SHARE_REPORT_TIME', '24H');
        # 将游标中的值再赋值给变量，供下次循环使用
        FETCH project_curosr INTO projectId;
    UNTIL end_flag END REPEAT;

    #关闭游标
    CLOSE project_curosr;

END
//
DELIMITER ;

CALL project_ui_appl();
DROP PROCEDURE IF EXISTS project_ui_appl;


-- V130__2-2-0_api_custom_field
-- 工单名称: V130__2-2-0_api_custom_field
-- 创建人: wangxiaogang
-- 创建时间: 2022-09-14 16:06:26
-- 工单描述 接口增加自定义模版
#设置权限
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_TEMPLATE:READ+API_TEMPLATE', 'PROJECT_TEMPLATE');


#创建api自定义字段表
CREATE TABLE IF NOT EXISTS custom_field_api
(
    resource_id varchar(50) NOT NULL,
    field_id    varchar(50) NOT NULL,
    value       varchar(500),
    text_value  text,
    PRIMARY KEY (resource_id, field_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


#创建api模版表
CREATE TABLE `api_template` (
    `id` varchar(100) NOT NULL,
    `name` varchar(64) NOT NULL COMMENT 'Field template name',
    `description` varchar(255) DEFAULT NULL COMMENT 'Field template description',
    `system` tinyint(1) DEFAULT '0' COMMENT 'Is system field template ',
    `global` tinyint(1) DEFAULT '0' COMMENT 'Is global template',
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time` bigint(13) NOT NULL COMMENT 'Update timestamp',
    `create_user` varchar(100) DEFAULT NULL,
    `project_id` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;



#系统默认api模版
INSERT INTO api_template (id,name,description,`system`,`global`,create_time,update_time, create_user,project_id)
VALUES ('1bb1fae3-5d83-45a1-80ab-31135d91de39','default','Api default template.',1,1,unix_timestamp() * 1000,unix_timestamp() * 1000,'','global');


#项目表增加api模版字段
ALTER TABLE project ADD api_template_id VARCHAR(64) NULL;



#给项目关联默认接口模板
UPDATE project set api_template_id = '1bb1fae3-5d83-45a1-80ab-31135d91de39';


--
-- V130__2-2-0_custom_field_template
-- 工单名称 V130__2-2-0_custom_field_template
-- 创建人 wangxiaogang
-- 创建时间 2022-09-15 16:41:24
-- 工单描述 修改自定义字段模版表的默认值类型为text
ALTER TABLE custom_field_template MODIFY default_value TEXT NULL;

ALTER TABLE test_case_template MODIFY prerequisite TEXT NULL;


--
-- V131__2-2-0_repository_file
-- 工单名称 V131__2-2-0_repository_file
-- 创建人 jianguo
-- 创建时间 2022-09-16 18:56:28
ALTER TABLE `file_module` ADD `module_type` varchar(20) DEFAULT 'module' COMMENT '模块类型: module/repository';

ALTER TABLE `file_module` ADD `repository_path` varchar(255) COMMENT '存储库路径';

ALTER TABLE `file_module` ADD `repository_user_name` varchar(255) COMMENT '存储库Token';

ALTER TABLE `file_module` ADD `repository_token` varchar(255) COMMENT '存储库Token';

ALTER TABLE `file_module` ADD `repository_desc` LONGTEXT COMMENT '存储库描述';


ALTER TABLE `file_metadata` ADD `latest` tinyint(1) DEFAULT 1 COMMENT '是否是最新版';

ALTER TABLE `file_metadata` ADD `ref_id` VARCHAR(50) COMMENT '同版本数据关联的ID';

ALTER TABLE `file_metadata` ADD `attach_info` LONGTEXT COMMENT '附加信息 如果storage为GIT 则存放的是commitID等一系列json数据';


ALTER TABLE `file_metadata` ADD INDEX `INDEX_LATEST`(`latest`);

ALTER TABLE `file_metadata` ADD INDEX `INDEX_STORAGE`(`storage`);

ALTER TABLE `file_metadata` ADD INDEX `INDEX_REF_ID`(`ref_id`);


--
-- V132__2-2-0_feat_custom_command
-- 工单名称 V132__2-2-0_feat_custom_command
-- 创建人 liuyao
-- 创建时间 2022-09-17 03:04:23
-- 工单描述 V2.2 自定义指令需求字段添加

ALTER TABLE `ui_scenario` ADD COLUMN scenario_type VARCHAR(100) NOT NULL DEFAULT 'scenario' COMMENT 'Scenario type'  AFTER `level`;

ALTER TABLE `ui_scenario_module` ADD COLUMN scenario_type VARCHAR(100) NOT NULL DEFAULT 'scenario' COMMENT 'Scenario type' AFTER `level`;

-- 功能用例, 缺陷管理文件管理关联字段
ALTER TABLE `attachment_module_relation` MODIFY COLUMN attachment_id VARCHAR(50) NULL;

ALTER TABLE `attachment_module_relation` ADD COLUMN file_metadata_ref_id VARCHAR(50) DEFAULT NULL COMMENT 'FILE ASSOCIATION ID';

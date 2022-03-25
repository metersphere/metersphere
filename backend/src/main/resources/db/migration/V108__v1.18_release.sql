ALTER TABLE `operating_log` ADD INDEX oper_time_index ( `oper_time` );

CREATE TABLE `operating_log_resource`
(
    `id` varchar(50) NOT NULL COMMENT 'ID',
    `operating_log_id`   varchar(50) NOT NULL COMMENT 'Operating log ID',
    `source_id`    varchar(50) NOT NULL COMMENT 'operating source id',
    PRIMARY KEY (`id`),
 KEY `operating_log_id_index` (`operating_log_id`),
 KEY `source_id_index` (`source_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- 操作日志
INSERT INTO operating_log_resource ( id, operating_log_id, source_id )
SELECT id, id AS operating_log_id, TRIM( BOTH '"' FROM source_id ) AS source_id
FROM operating_log
WHERE
	source_id != ''
	AND oper_type = 'UPDATE'
	AND oper_module IN (
		'API_AUTOMATION',
		'接口自动化',
		'Api automation',
		'接口自動化',
		'API_AUTOMATION',
		'接口定义',
		'接口定義',
		'Api definition',
		'API_DEFINITION',
		'接口定义用例',
		'接口定義用例',
		'Api definition case',
		'API_DEFINITION_CASE',
		'性能测试',
		'性能測試',
		'Performance test',
		'PERFORMANCE_TEST',
		'测试用例',
		'測試用例',
		'Test case',
		'TRACK_TEST_CASE'
	);

CREATE TABLE IF NOT EXISTS `project_application`
(
    `project_id` varchar(50) DEFAULT NULL,
    `type` varchar(50) DEFAULT NULL,
    `type_value` varchar(255) DEFAULT NULL,
    UNIQUE KEY `project_application_pk` (`project_id`, `type`),
    KEY `project_application_project_id_index` (`project_id`),
    KEY `project_application_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- permission
DROP PROCEDURE IF EXISTS test_personal;
DELIMITER //
CREATE PROCEDURE test_personal()
    BEGIN
        #声明结束标识
        DECLARE end_flag int DEFAULT 0;

        DECLARE groupId varchar(64);

        #声明游标 group_curosr
        DECLARE group_curosr CURSOR FOR SELECT DISTINCT group_id FROM user_group;

        #设置终止标志
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET end_flag=1;

        #打开游标
        OPEN group_curosr;
            #获取当前游标指针记录，取出值赋给自定义的变量
            FETCH group_curosr INTO groupId;
            #遍历游标
            REPEAT

                #利用取到的值进行数据库的操作
                INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
                VALUES (uuid(), groupId, 'PERSONAL_INFORMATION:READ+EDIT', 'PERSONAL_INFORMATION'),
                       (uuid(), groupId, 'PERSONAL_INFORMATION:READ+THIRD_ACCOUNT', 'PERSONAL_INFORMATION'),
                       (uuid(), groupId, 'PERSONAL_INFORMATION:READ+API_KEYS', 'PERSONAL_INFORMATION'),
                       (uuid(), groupId, 'PERSONAL_INFORMATION:READ+EDIT_PASSWORD', 'PERSONAL_INFORMATION');
                # 根据 end_flag 判断是否结束
                # 将游标中的值再赋值给变量，供下次循环使用
                FETCH group_curosr INTO groupId;
            UNTIL end_flag END REPEAT;

        #关闭游标
        close group_curosr;

    END
//
DELIMITER ;

CALL test_personal();
DROP PROCEDURE IF EXISTS test_personal;

-- 测试计划缺陷与用例缺陷分离
ALTER TABLE test_case_issues ADD ref_type varchar(30) DEFAULT 'FUNCTIONAL' NOT NULL;
ALTER TABLE test_case_issues CHANGE test_case_id resource_id varchar(50) NOT NULL;
ALTER TABLE test_case_issues ADD ref_id varchar(50) NULL COMMENT '测试计划的用例所指向的用例的id';

INSERT INTO test_case_issues (id, resource_id, issues_id, ref_id, ref_type)
    SELECT uuid(), tptc.id, i.id, tptc.case_id, 'PLAN_FUNCTIONAL'
    FROM issues i
             INNER JOIN test_case_issues tci
                        ON tci.issues_id = i.id
             INNER JOIN test_plan_test_case tptc
                        ON tci.resource_id = tptc.case_id AND i.resource_id = tptc.plan_id;

DELETE FROM test_case_issues WHERE id IN (
    SELECT id FROM (
                       SELECT tci.id AS id
                       FROM issues i
                       INNER JOIN test_case_issues tci
                           ON tci.issues_id = i.id
                       INNER JOIN test_plan_test_case tptc
                           ON tci.resource_id = tptc.case_id AND i.resource_id = tptc.plan_id
                   ) tmp
);


DROP PROCEDURE IF EXISTS project_appl;
DELIMITER //
CREATE PROCEDURE project_appl()
BEGIN
    #声明结束标识
    DECLARE end_flag int DEFAULT 0;

    DECLARE projectId varchar(64);

    #声明游标 group_curosr
    DECLARE project_curosr CURSOR FOR SELECT DISTINCT id FROM project;

    #设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET end_flag=1;

    #打开游标
    OPEN project_curosr;
        #获取当前游标指针记录，取出值赋给自定义的变量
        FETCH project_curosr INTO projectId;
        #遍历游标
        REPEAT
            #利用取到的值进行数据库的操作
            INSERT INTO project_application (project_id, type, type_value)
            VALUES (projectId, 'TRACK_SHARE_REPORT_TIME', '24H'),
                   (projectId, 'PERFORMANCE_SHARE_REPORT_TIME', '24H');
            # 将游标中的值再赋值给变量，供下次循环使用
            FETCH project_curosr INTO projectId;
        UNTIL end_flag END REPEAT;

    #关闭游标
    close project_curosr;

END
//
DELIMITER ;

CALL project_appl();
DROP PROCEDURE IF EXISTS project_appl;

ALTER TABLE api_definition_exec_result
    ADD project_id varchar(50);

ALTER TABLE api_definition_exec_result
    ADD integrated_report_id varchar(50);

ALTER TABLE `api_definition_exec_result`
    ADD INDEX project_id_index ( `project_id` );

ALTER TABLE api_scenario_report
    ADD report_type varchar(100);

update api_definition_exec_result t
    INNER JOIN
    (select id,project_id from api_test_case) atc on t.resource_id = atc.id
set t.project_id = atc.project_id where atc.id = t.resource_id;

update api_scenario_report set report_type = 'SCENARIO_INTEGRATED' where scenario_id like '[%';
update api_scenario_report set report_type = 'SCENARIO_INDEPENDENT' where report_type is null;

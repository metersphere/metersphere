CREATE TABLE IF NOT EXISTS `api_definition_scenario_relevance`
(
    `report_id` varchar(50) NOT NULL COMMENT 'ID',
    PRIMARY KEY (`report_id`),
    KEY `relevance_report_id_index` (`report_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `api_scenario_report` ADD INDEX update_time_index ( `update_time` );

ALTER TABLE `api_definition_exec_result` ADD INDEX update_time_index ( `create_time` );

ALTER TABLE `api_definition_exec_result` ADD INDEX integrated_report_id_index ( `integrated_report_id` );


INSERT INTO api_definition_scenario_relevance (report_id)
SELECT s_r.id
FROM api_scenario_report s_r
where  s_r.execute_type = 'Saved';

INSERT INTO api_definition_scenario_relevance (report_id)
select a_r.id
from api_definition_exec_result a_r
where (a_r.integrated_report_id is null or a_r.integrated_report_id = 'null');






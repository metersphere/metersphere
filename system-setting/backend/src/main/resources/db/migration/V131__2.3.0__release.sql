-- v2_3 init
-- V2_3_micro_service_module
-- 工单名称: V2_3_micro_service_module
-- 创建人: captain
-- 创建时间: 2022-09-21 15:47:07
-- 工单描述: 微服务相关

INSERT INTO system_parameter (param_key, param_value, type, sort) VALUES ('metersphere.module.report', 'ENABLE', 'text', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort) VALUES ('metersphere.module.track', 'ENABLE', 'text', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort) VALUES ('metersphere.module.project', 'ENABLE', 'text', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort) VALUES ('metersphere.module.setting', 'ENABLE', 'text', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort) VALUES ('base.grid.concurrency', '4', 'text', 1);

DELETE FROM user_group_permission WHERE permission_id = 'WORKSPACE_PROJECT_MANAGER:READ+UPLOAD_JAR';

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
SELECT UUID(), id, 'WORKSPACE_PROJECT_MANAGER:READ+ENVIRONMENT_CONFIG', 'WORKSPACE_PROJECT_MANAGER'
FROM `group`
WHERE type = 'WORKSPACE';

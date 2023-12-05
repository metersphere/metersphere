-- 模拟数据
INSERT INTO organization (id, num, name, description, create_user, update_user, create_time, update_time) VALUES ('wx_organization_id_001', null, '测试日志组织', '测试日志的组织', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);

INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES ('wx_project_id_001', null, 'wx_organization_id_001', '测试日志项目', '测试日志的项目', 'admin', 'admin', unix_timestamp() * 1000, unix_timestamp() * 1000);


-- 初始化日志记录
INSERT INTO operation_log( `id`, `project_id`, `organization_id`, `create_time`, `create_user`, `source_id`, `method`, `type`, `module`, `content`, `path`) VALUES (10001, 'wx_project_id_001', 'wx_organization_id_001', 1689141859000, 'admin', '1', 'post', 'add', 'TEST_FUNCTIONAL_CASE', '用例', '/functionalCase/add');
INSERT INTO operation_log( `id`, `project_id`, `organization_id`, `create_time`, `create_user`, `source_id`, `method`, `type`, `module`, `content`, `path`) VALUES (10002, 'wx_project_id_001', 'wx_organization_id_001', 1689141859000, 'admin', '1', 'post', 'update', 'TEST_FUNCTIONAL_CASE', '用例', '/functionalCase/update');
INSERT INTO operation_log( `id`, `project_id`, `organization_id`, `create_time`, `create_user`, `source_id`, `method`, `type`, `module`, `content`, `path`) VALUES (10003, 'wx_project_id_001', 'wx_organization_id_001', 1689141859000, 'admin', '1', 'post', 'update', 'TEST_FUNCTIONAL_CASE', '用例', '/functionalCase/update');
INSERT INTO operation_log( `id`, `project_id`, `organization_id`, `create_time`, `create_user`, `source_id`, `method`, `type`, `module`, `content`, `path`) VALUES (10007, 'wx_project_id_001', 'wx_organization_id_001', 1689141859000, 'admin', '2', 'post', 'update', 'TEST_FUNCTIONAL_CASE', '用例', '/functionalCase/update');
INSERT INTO operation_log( `id`, `project_id`, `organization_id`, `create_time`, `create_user`, `source_id`, `method`, `type`, `module`, `content`, `path`) VALUES (10005, 'wx_project_id_001', 'wx_organization_id_001', 1689141859000, 'admin', '2', 'post', 'update', 'TEST_FUNCTIONAL_CASE', '用例', '/functionalCase/update');
INSERT INTO operation_log( `id`, `project_id`, `organization_id`, `create_time`, `create_user`, `source_id`, `method`, `type`, `module`, `content`, `path`) VALUES (10006, 'wx_project_id_001', 'wx_organization_id_001', 1689141859000, 'admin', '3', 'post', 'update', 'TEST_FUNCTIONAL_CASE', '用例', '/functionalCase/update');


INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10001, 'wx_project_id_001', 1699866679639, 'admin', '1', 'ADD', 'FUNCTIONAL_CASE');
INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10002, 'wx_project_id_001', 1699866679639, 'admin', '1', 'UPDATE', 'FUNCTIONAL_CASE');
INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10003, 'wx_project_id_001', 1699866679639, 'admin', '1', 'UPDATE', 'FUNCTIONAL_CASE');
INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10004, 'wx_project_id_001', 1699866679639, 'admin', '1', 'UPDATE', 'FUNCTIONAL_CASE');
INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10005, 'wx_project_id_001', 1699866679639, 'admin', '2', 'UPDATE', 'FUNCTIONAL_CASE');
INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10006, 'wx_project_id_001', 1699866679639, 'admin', '2', 'UPDATE', 'FUNCTIONAL_CASE');
INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10007, 'wx_project_id_001', 1699866679639, 'admin', '2', 'ADD', 'FUNCTIONAL_CASE');
INSERT INTO operation_history(id, project_id, create_time, create_user, source_id, type, module) VALUES (10008, 'wx_project_id_001', 1699866679639, 'admin', '3', 'ADD', 'FUNCTIONAL_CASE');

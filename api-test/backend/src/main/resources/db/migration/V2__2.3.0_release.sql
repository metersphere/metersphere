-- 场景报告状态统一调整
-- 失败状态
update api_scenario_report set status ='ERROR' where status in ('Error','Timeout','Fail');
-- 成功状态
update api_scenario_report set status ='SUCCESS' where status = 'Success';
-- 未执行
update api_scenario_report set status ='PENDING' where status = 'unexecute';
-- 已停止
update api_scenario_report set status ='STOPPED' where status = 'STOP';
-- 误报
update api_scenario_report set status ='FAKE_ERROR' where status in ('errorReportResult','errorReport');
-- 执行中
update api_scenario_report set status ='RUNNING' where status = 'Running';

-- 场景报告结果状态统一调整
-- 失败状态
update api_scenario_report_result set status ='ERROR' where status in ('Error','Timeout','Fail');
-- 成功状态
update api_scenario_report_result set status ='SUCCESS' where status = 'Success';
-- 未执行
update api_scenario_report_result set status ='PENDING' where status = 'unexecute';
-- 已停止
update api_scenario_report_result set status ='STOPPED' where status = 'STOP';
-- 误报
update api_scenario_report_result set status ='FAKE_ERROR' where status in ('errorReportResult','errorReport');
-- 执行中
update api_scenario_report_result set status ='RUNNING' where status = 'Running';

-- 接口用例结果状态统一调整
-- 失败状态
update api_definition_exec_result set status ='ERROR' where status in ('Error','Timeout','Fail');
-- 成功状态
update api_definition_exec_result set status ='SUCCESS' where status = 'Success';
-- 未执行
update api_definition_exec_result set status ='PENDING' where status = 'unexecute';
-- 已停止
update api_definition_exec_result set status ='STOPPED' where status = 'STOP';
-- 误报
update api_definition_exec_result set status ='FAKE_ERROR' where status in ('errorReportResult','errorReport');
-- 执行中
update api_definition_exec_result set status ='RUNNING' where status = 'Running';

-- 场景结果状态
update api_scenario set last_result ='ERROR' where last_result in ('Error','Timeout','Fail');

update api_scenario set last_result ='SUCCESS' where last_result = 'Success';
-- 未执行
update api_scenario set last_result ='PENDING' where last_result = 'unexecute';
-- 已停止
update api_scenario set last_result ='STOPPED' where last_result = 'STOP';
-- 误报
update api_scenario set last_result ='FAKE_ERROR' where last_result in ('errorReportResult','errorReport');
-- 执行中
update api_scenario set last_result ='RUNNING' where last_result = 'Running';

-- 用例状态
update api_test_case set `status` ='ERROR' where `status` in ('Error','Timeout','Fail');

update api_test_case set `status` ='SUCCESS' where `status` = 'Success';
-- 未执行
update api_test_case set `status` ='PENDING' where `status` = 'unexecute';
-- 已停止
update api_test_case set `status` ='STOPPED' where `status` = 'STOP';
-- 误报
update api_test_case set `status` ='FAKE_ERROR' where `status` in ('errorReportResult','errorReport');
-- 执行中
update api_test_case set `status` ='RUNNING' where `status` = 'Running';

-- 测试计划场景结果状态
update test_plan_api_scenario set last_result ='ERROR' where last_result in ('Error','Timeout','Fail');

update test_plan_api_scenario set last_result ='SUCCESS' where last_result = 'Success';
-- 未执行
update test_plan_api_scenario set last_result ='PENDING' where last_result = 'unexecute';
-- 已停止
update test_plan_api_scenario set last_result ='STOPPED' where last_result = 'STOP';
-- 误报
update test_plan_api_scenario set last_result ='FAKE_ERROR' where last_result in ('errorReportResult','errorReport');
-- 执行中
update test_plan_api_scenario set last_result ='RUNNING' where last_result = 'Running';

-- 测试计划接口用例状态
update test_plan_api_case set `status` ='ERROR' where `status` in ('Error','Timeout','Fail');

update test_plan_api_case set `status` ='SUCCESS' where `status` = 'Success';
-- 未执行
update test_plan_api_case set `status` ='PENDING' where `status` = 'unexecute';
-- 已停止
update test_plan_api_case set `status` ='STOPPED' where `status` = 'STOP';
-- 误报
update test_plan_api_case set `status` ='FAKE_ERROR' where `status` in ('errorReportResult','errorReport');
-- 执行中
update test_plan_api_case set `status` ='RUNNING' where `status` = 'Running';

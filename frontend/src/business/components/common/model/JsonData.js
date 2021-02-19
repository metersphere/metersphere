import i18n from '../../../../i18n/i18n'
//自定义默认表头
//测试用例
export const Track_Test_Case = [
  {prop: 'num', label: i18n.t('commons.id')},
  {prop: 'name', label: i18n.t('commons.name')},
  {prop: 'priority', label: i18n.t('test_track.case.priority')},
  {prop: 'type', label: i18n.t('test_track.case.type')},
  {prop: 'method', label: i18n.t('test_track.case.method')},
  {prop: 'reviewStatus', label: i18n.t('test_track.case.status')},
  {prop: 'tags', label: i18n.t('commons.tag')},
  {prop: 'nodePath', label: i18n.t('test_track.case.module')},
  {prop: 'updateTime', label: i18n.t('commons.update_time')},
]
//用例评审-测试用例
export const Test_Case_Review = [
  {prop: 'name', label: i18n.t('test_track.review.review_name')},
  {prop: 'reviewer', label: i18n.t('test_track.review.reviewer')},
  {prop: 'projectName', label: i18n.t('test_track.review.review_project')},
  {prop: 'creatorName', label: i18n.t('test_track.review.review_creator')},
  {prop: 'status', label: i18n.t('test_track.review.review_status')},
  {prop: 'createTime', label: i18n.t('commons.create_time')},
  {prop: 'endTime', label: i18n.t('test_track.review.end_time')},
]
//测试计划-测试用例
export const Test_Plan_List = [
  {prop: 'name', label: i18n.t('commons.name')},
  {prop: 'userName', label: i18n.t('test_track.plan.plan_principal')},
  {prop: 'status', label: i18n.t('test_track.plan.plan_status')},
  {prop: 'stage', label: i18n.t('test_track.plan.plan_stage')},
  {prop: 'testRate', label: i18n.t('test_track.home.test_rate')},
  {prop: 'projectName', label: i18n.t('test_track.plan.plan_project')},
  {prop: 'plannedStartTime', label: i18n.t('test_track.plan.planned_start_time')},
  {prop: 'plannedEndTime', label: i18n.t('test_track.plan.planned_end_time')},
  {prop: 'actualStartTime', label: i18n.t('test_track.plan.actual_start_time')},
  {prop: 'actualEndTime', label: i18n.t('test_track.plan.actual_end_time')},
]
//接口定义-api列表
export const Api_List = [
  {prop: 'num', label: "ID"},
  {prop: 'name', label: i18n.t('api_test.definition.api_name')},
  {prop: 'method', label: i18n.t('api_test.definition.api_type')},
  {prop: 'userName', label: i18n.t('api_test.definition.api_principal')},
  {prop: 'path', label: i18n.t('api_test.definition.api_path')},
  {prop: 'tags', label: i18n.t('commons.tag')},
  {prop: 'updateTime', label: i18n.t('api_test.definition.api_last_time')},
  {prop: 'caseTotal', label: i18n.t('api_test.definition.api_case_number')},
  {prop: 'caseStatus', label: i18n.t('api_test.definition.api_case_status')},
  {prop: 'casePassingRate', label: i18n.t('api_test.definition.api_case_passing_rate')},
]
//接口定义-case列表
export const Api_Case_List = [
  {prop: 'num', label: "ID"},
  {prop: 'name', label: i18n.t('test_track.case.name')},
  {prop: 'priority', label: i18n.t('test_track.case.priority')},
  {prop: 'path', label: i18n.t('api_test.definition.api_path')},
  {prop: 'tags', label: i18n.t('commons.tag')},
  {prop: 'createUser', label: "创建人"},
  {prop: 'updateTime', label: i18n.t('api_test.definition.api_last_time')},
]
//接口自动化-场景列表
export const Api_Scenario_List = [
  {prop: 'num', label: "ID"},
  {prop: 'name', label: i18n.t('test_track.case.name')},
  {prop: 'priority', label: i18n.t('test_track.case.priority')},
  {prop: 'path', label: i18n.t('api_test.definition.api_path')},
  {prop: 'tags', label: i18n.t('commons.tag')},
  {prop: 'createUser', label: '创建人'},
  {prop: 'updateTime', label: i18n.t('api_test.definition.api_last_time')},
]
//测试评审-测试用例
export const Test_Case_Review_Case_List = [
  {prop: 'num', label: i18n.t('commons.id')},
  {prop: 'name', label: i18n.t('commons.name')},
  {prop: 'priority', label: i18n.t('test_track.case.priority')},
  {prop: 'type', label: i18n.t('test_track.case.type')},
  {prop: 'method', label: i18n.t('test_track.case.method')},
  {prop: 'nodePath', label: i18n.t('test_track.case.module')},
  {prop: 'projectName', label: i18n.t('test_track.review.review_project')},
  {prop: 'reviewerName', label: i18n.t('test_track.review.reviewer')},
  {prop: 'reviewStatus', label: i18n.t('test_track.case.status')},
  {prop: 'updateTime', label: i18n.t('commons.update_time')},
]
//测试计划-功能用例
export const Test_Plan_Function_Test_Case = [
  {prop: 'num', label: i18n.t('commons.id')},
  {prop: 'name', label: i18n.t('commons.name')},
  {prop: 'priority', label: i18n.t('test_track.case.priority')},
  {prop: 'type', label: i18n.t('test_track.case.type')},
  {prop: 'tags', label: i18n.t('commons.tag')},
  {prop: 'method', label: i18n.t('test_track.case.method')},
  {prop: 'nodePath', label: i18n.t('test_track.case.module')},
  {prop: 'projectName', label: i18n.t('test_track.review.review_project')},
  {prop: 'issuesContent', label: i18n.t('test_track.issue.issue')},
  {prop: 'executorName', label: i18n.t('test_track.plan_view.executor')},
  {prop: 'status', label: i18n.t('test_track.plan_view.execute_result')},
  {prop: 'updateTime', label: i18n.t('commons.update_time')},
]
//测试计划-api用例
export const Test_Plan_Api_Case = [
  {prop: 'num', label: i18n.t('commons.id')},
  {prop: 'name', label: i18n.t('commons.name')},
  {prop: 'priority', label: i18n.t('test_track.case.priority')},
  {prop: 'path', label: i18n.t('api_test.definition.api_path')},
  {prop: 'createUser', label: '创建人'},
  {prop: 'custom', label: i18n.t('api_test.definition.api_last_time')},
  {prop: 'tags', label: i18n.t('commons.tag')},
  {prop: 'execResult', label: '执行状态'},
]
//测试计划-性能用例
export const Test_Plan_Load_Case = [
  {prop: 'num', label: i18n.t('commons.id')},
  {prop: 'caseName', label: i18n.t('commons.name')},
  {prop: 'projectName', label: i18n.t('load_test.project_name')},
  {prop: 'userName', label: i18n.t('load_test.user_name')},
  {prop: 'createTime', label: i18n.t('commons.create_time')},
  {prop: 'status', label: i18n.t('commons.status')},
  {prop: 'caseStatus', label: i18n.t('test_track.plan.load_case.execution_status')},
  {prop: 'loadReportId', label: i18n.t('test_track.plan.load_case.view_report')},
]
//测试计划-场景用例
export const Test_Plan_Scenario_Case = [
  {prop: 'num', label: i18n.t('commons.id')},
  {prop: 'name', label: i18n.t('commons.name')},
  {prop: 'level', label: i18n.t('api_test.automation.case_level')},
  {prop: 'tagNames', label: i18n.t('api_test.automation.tag')},
  {prop: 'userId', label: i18n.t('api_test.automation.creator')},
  {prop: 'updateTime', label: i18n.t('api_test.automation.update_time')},
  {prop: 'stepTotal', label: i18n.t('api_test.automation.success')},
  {prop: 'lastResult', label: i18n.t('api_test.automation.fail')},
  {prop: 'passRate', label: i18n.t('api_test.automation.passing_rate')},
]


import i18n from '../../../../i18n/i18n'
//自定义默认表头
//测试用例
export const Track_Test_Case = [
  {id: 'num', label: i18n.t('commons.id')},
  {id: 'name', label: i18n.t('commons.name')},
  {id: 'priority', label: i18n.t('test_track.case.priority')},
  {id: 'reviewStatus', label: i18n.t('test_track.case.status')},
  {id: 'tags', label: i18n.t('commons.tag')},
  {id: 'nodePath', label: i18n.t('test_track.case.module')},
  {id: 'updateTime', label: i18n.t('commons.update_time')},
  {id: 'createUser', label: i18n.t('commons.create_user')},
  {id: 'maintainer', label: i18n.t('custom_field.case_maintainer')},
]
//用例评审-测试用例
export const Test_Case_Review = [
  {id: 'name', label: i18n.t('test_track.review.review_name')},
  {id: 'reviewer', label: i18n.t('test_track.review.reviewer')},
  {id: 'projectName', label: i18n.t('test_track.review.review_project')},
  {id: 'creatorName', label: i18n.t('test_track.review.review_creator')},
  {id: 'status', label: i18n.t('test_track.review.review_status')},
  {id: 'createTime', label: i18n.t('commons.create_time')},
  {id: 'endTime', label: i18n.t('test_track.review.end_time')},
  {id: 'tags', label: '标签'},
]
//测试计划-测试用例
export const Test_Plan_List = [
  {id: 'name', label: i18n.t('commons.name')},
  {id: 'userName', label: i18n.t('test_track.plan.plan_principal')},
  {id: 'status', label: i18n.t('test_track.plan.plan_status')},
  {id: 'stage', label: i18n.t('test_track.plan.plan_stage')},
  {id: 'testRate', label: i18n.t('test_track.home.test_rate')},
  {id: 'projectName', label: i18n.t('test_track.plan.plan_project')},
  {id: 'plannedStartTime', label: i18n.t('test_track.plan.planned_start_time')},
  {id: 'plannedEndTime', label: i18n.t('test_track.plan.planned_end_time')},
  {id: 'actualStartTime', label: i18n.t('test_track.plan.actual_start_time')},
  {id: 'actualEndTime', label: i18n.t('test_track.plan.actual_end_time')},
  {id: 'tags', label: i18n.t('commons.tag')},
  {id: 'executionTimes', label: i18n.t('commons.execution_times')},
  {id: 'passRate', label: i18n.t('commons.pass_rate')},
  {id: 'createUser', label: i18n.t('commons.create_user')},
]
//接口定义-api列表
export const Api_List = [
  {id: 'num', label: "ID"},
  {id: 'name', label: i18n.t('api_test.definition.api_name')},
  {id: 'method', label: i18n.t('api_test.definition.api_type')},
  {id: 'userName', label: i18n.t('api_test.definition.api_principal')},
  {id: 'path', label: i18n.t('api_test.definition.api_path')},
  {id: 'tags', label: i18n.t('commons.tag')},
  {id: 'updateTime', label: i18n.t('api_test.definition.api_last_time')},
  {id: 'caseTotal', label: i18n.t('api_test.definition.api_case_number')},
  {id: 'caseStatus', label: i18n.t('api_test.definition.api_case_status')},
  {id: 'casePassingRate', label: i18n.t('api_test.definition.api_case_passing_rate')},
  {id: 'status', label: i18n.t('api_test.definition.api_status')}
]


//测试评审-测试用例
export const Test_Case_Review_Case_List = [
  {id: 'num', label: i18n.t('commons.id')},
  {id: 'name', label: i18n.t('commons.name')},
  {id: 'priority', label: i18n.t('test_track.case.priority')},
  {id: 'type', label: i18n.t('test_track.case.type')},
  {id: 'nodePath', label: i18n.t('test_track.case.module')},
  {id: 'projectName', label: i18n.t('test_track.review.review_project')},
  {id: 'reviewerName', label: i18n.t('test_track.review.reviewer')},
  {id: 'reviewStatus', label: i18n.t('test_track.case.status')},
  {id: 'updateTime', label: i18n.t('commons.update_time')},
  {id: 'maintainer', label: i18n.t('custom_field.case_maintainer')},
]
//测试计划-功能用例
export const Test_Plan_Function_Test_Case = [
  {id: 'num', label: i18n.t('commons.id')},
  {id: 'name', label: i18n.t('commons.name')},
  {id: 'priority', label: i18n.t('test_track.case.priority')},
  {id: 'type', label: i18n.t('test_track.case.type')},
  {id: 'tags', label: i18n.t('commons.tag')},
  {id: 'nodePath', label: i18n.t('test_track.case.module')},
  {id: 'projectName', label: i18n.t('test_track.review.review_project')},
  {id: 'issuesContent', label: i18n.t('test_track.issue.issue')},
  {id: 'executorName', label: i18n.t('test_track.plan_view.executor')},
  {id: 'status', label: i18n.t('test_track.plan_view.execute_result')},
  {id: 'updateTime', label: i18n.t('commons.update_time')},
  {id: 'maintainer', label: i18n.t('api_test.definition.request.responsible')}
]
//测试计划-api用例
export const Test_Plan_Api_Case = [
  {id: 'num', label: i18n.t('commons.id')},
  {id: 'name', label: i18n.t('api_test.definition.api_name')},
  {id: 'priority', label: i18n.t('test_track.case.priority')},
  {id: 'path', label: i18n.t('api_test.definition.api_path')},
  {id: 'createUser', label: '创建人'},
  {id: 'custom', label: i18n.t('api_test.definition.api_last_time')},
  {id: 'tags', label: i18n.t('commons.tag')},
  {id: 'execResult', label: '执行状态'},
  {id: 'maintainer', label: i18n.t('api_test.definition.request.responsible')},
  {id: 'createTime', label: i18n.t('commons.create_time')},


]
//测试计划-性能用例
export const Test_Plan_Load_Case = [
  {id: 'num', label: i18n.t('commons.id')},
  {id: 'caseName', label: i18n.t('commons.name')},
  {id: 'projectName', label: i18n.t('load_test.project_name')},
  {id: 'userName', label: i18n.t('load_test.user_name')},
  {id: 'createTime', label: i18n.t('commons.create_time')},
  {id: 'status', label: i18n.t('commons.status')},
  {id: 'caseStatus', label: i18n.t('test_track.plan.load_case.execution_status')},
  {id: 'loadReportId', label: i18n.t('test_track.plan.load_case.report')},
]
//测试计划-场景用例
export const Test_Plan_Scenario_Case = [
  {id: 'num', label: i18n.t('commons.id')},
  {id: 'name', label: i18n.t('api_test.automation.scenario_name')},
  {id: 'level', label: i18n.t('api_test.automation.case_level')},
  {id: 'tagNames', label: i18n.t('api_test.automation.tag')},
  {id: 'userId', label: i18n.t('api_test.automation.creator')},
  {id: 'updateTime', label: i18n.t('api_test.automation.update_time')},
  {id: 'stepTotal', label: i18n.t('api_test.automation.success')},
  {id: 'lastResult', label: i18n.t('api_test.automation.fail')},
  {id: 'passRate', label: i18n.t('api_test.automation.passing_rate')},
  {id: 'maintainer', label: i18n.t('api_test.definition.request.responsible')},
  {id: 'createTime', label: i18n.t('commons.create_time')},


]


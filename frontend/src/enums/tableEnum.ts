export enum TableOpenDetailModeEnum {
  DRAWER = 'drawer',
  NEW_WINDOW = 'new_window',
}

export enum TableModuleEnum {
  USERGROUPINDEX = 'userGroupIndex',
}

export enum TableKeyEnum {
  API_TEST = 'apiTest',
  API_TEST_MANAGEMENT_CASE = 'apiTestManagementCase',
  API_TEST_MANAGEMENT_MOCK = 'apiTestManagementMock',
  API_TEST_DEBUG_FORM_DATA = 'apiTestDebugFormData',
  API_TEST_DEBUG_REST = 'apiTestDebugRest',
  API_TEST_DEBUG_QUERY = 'apiTestDebugQuery',
  API_TEST_DEBUG_FORM_URL_ENCODE = 'apiTestDebugFormUrlEncoded',
  API_TEST_REPORT = 'apiTestReport',
  API_SCENARIO = 'apiScenario',
  SYSTEM_USER = 'systemUser',
  SYSTEM_RESOURCEPOOL = 'systemResourcePool',
  SYSTEM_AUTH = 'systemAuth',
  SYSTEM_ORGANIZATION = 'systemOrganization',
  SYSTEM_PROJECT = 'systemProject',
  SYSTEM_LOG = 'systemLog',
  PROJECT_MEMBER = 'projectMember',
  PROJECT_USER_GROUP = 'projectUserGroup',
  ORGANIZATION_MEMBER = 'organizationMember',
  ORGANIZATION_TEMPLATE_FIELD_SETTING = 'organizationTemplateFieldSetting',
  ORGANIZATION_TEMPLATE_MANAGEMENT = 'organizationTemplateManagement',
  ORGANIZATION_TEMPLATE_MANAGEMENT_FIELD = 'organizationTemplateManagementField',
  ORGANIZATION_TEMPLATE_MANAGEMENT_STEP = 'organizationTemplateManagementStep',
  ORGANIZATION_PROJECT = 'organizationProject',
  ORGANIZATION_PROJECT_USER_DRAWER = 'organizationProjectUserDrawer',
  ORGANIZATION_PROJECT_COMMON_SCRIPT = 'projectManagementCommonScript',
  FILE_MANAGEMENT_FILE = 'fileManagementFile',
  FILE_MANAGEMENT_CASE = 'fileManagementCase',
  FILE_MANAGEMENT_CASE_RECYCLE = 'fileManagementCaseRecycle',
  FILE_MANAGEMENT_VERSION = 'fileManagementVersion',
  PROJECT_MANAGEMENT_MENU_FALSE_ALERT = 'projectManagementMenuFalseAlert',
  PROJECT_MANAGEMENT_COMMON_SCRIPT_DETAIL = 'projectManagementCommonScriptDetail',
  PROJECT_MANAGEMENT_COMMON_SCRIPT_CHANGE_HISTORY = 'projectManagementCommonScriptChangeHistory',
  ORGANIZATION_TEMPLATE_DEFECT_TABLE = 'organizationTemplateManagementDefect',
  CASE_MANAGEMENT_TABLE = 'caseManagement',
  CASE_MANAGEMENT_RECYCLE_TABLE = 'caseRecycleManagement',
  CASE_MANAGEMENT_DETAIL_TABLE = 'caseManagementDetailTable',
  CASE_MANAGEMENT_ASSOCIATED_TABLE = 'caseManagementAssociatedFileTable',
  BUG_MANAGEMENT = 'bugManagement',
  BUG_MANAGEMENT_RECYCLE = 'bugManagementRecycle',
  CASE_MANAGEMENT_REVIEW = 'caseManagementReview',
  CASE_MANAGEMENT_REVIEW_CASE = 'caseManagementReviewCase',
  CASE_MANAGEMENT_TAB_DEFECT = 'caseManagementTabDefect',
  // CASE_MANAGEMENT_TAB_DEFECT_TEST_PLAN = 'caseManagementTabTestPlan',
  CASE_MANAGEMENT_TAB_DEPENDENCY_PRE_CASE = 'caseManagementTabPreDependency',
  CASE_MANAGEMENT_TAB_DEPENDENCY_POST_CASE = 'caseManagementTabPostDependency',
  CASE_MANAGEMENT_TAB_REVIEW = 'caseManagementTabCaseReview',
  CASE_MANAGEMENT_TAB_TEST_PLAN = 'caseManagementTabTestPlan',
  CASE_MANAGEMENT_TAB_CHANGE_HISTORY = 'caseManagementTabChangeHistory',
  CASE_MANAGEMENT_TAB_CASE_TABLE = 'caseManagementTabCaseTable',
  CASE_MANAGEMENT_TAB_DEMAND_PLATFORM = 'caseManagementTabDemandPlatformTable',
  PROJECT_MANAGEMENT_ENV_ALL_PARAM = 'projectManagementEnvAllParam',
  PROJECT_MANAGEMENT_ENV_ENV_PARAM = 'projectManagementEnvEnvParam',
  PROJECT_MANAGEMENT_ENV_ENV_HTTP = 'projectManagementEnvEnvHttp',
  PROJECT_MANAGEMENT_ENV_ENV_DATASTORES = 'projectManagementEnvEnvDataSource',
  PROJECT_MANAGEMENT_ENV_ALL_PARAM_HEADER = 'projectManagementEnvAllParamHeader',
  PROJECT_MANAGEMENT_ENV_ALL_PARAM_VARIABLE = 'projectManagementEnvAllParamVariable',
  TEST_PLAN_ALL_TABLE = 'testPlanAllTable',
  TEST_PLAN_DETAIL_BUG_TABLE = 'testPlanDetailBug',
  TEST_PLAN_DETAIL_FEATURE_CASE_TABLE = 'testPlanDetailFeatureCaseTable',
  TEST_PLAN_DETAIL_BUG_TABLE_CASE_COUNT = 'testPlanDetailBugCaseCount',
  TEST_PLAN_DETAIL_CASE_TABLE_BUG_COUNT = 'testPlanDetailCaseBugCount',
  TEST_PLAN_DETAIL_API_CASE = 'testPlanDetailApiCase',
  TEST_PLAN_DETAIL_API_SCENARIO = 'testPlanDetailApiScenario',
  TEST_PLAN_REPORT_TABLE = 'testPlanReportTable',
  TASK_API_CASE_SYSTEM = 'taskCenterApiCaseSystem',
  TASK_API_CASE_ORGANIZATION = 'taskCenterApiCaseOrganization',
  TASK_API_CASE_PROJECT = 'taskCenterApiCaseProject',
  TASK_SCHEDULE_TASK_API_IMPORT_SYSTEM = 'taskCenterScheduleApiImportSystem',
  TASK_SCHEDULE_TASK_API_SCENARIO_SYSTEM = 'taskCenterScheduleApiScenarioSystem',
  TASK_SCHEDULE_TASK_API_IMPORT_ORGANIZATION = 'taskCenterScheduleApiImportOrganization',
  TASK_SCHEDULE_TASK_API_SCENARIO_ORGANIZATION = 'taskCenterScheduleApiScenarioOrganization',
  TASK_SCHEDULE_TASK_API_IMPORT_PROJECT = 'taskCenterScheduleApiImportProject',
  TASK_SCHEDULE_TASK_API_SCENARIO_PROJECT = 'taskCenterScheduleApiScenarioProject',
  TASK_SCHEDULE_TASK_TEST_PLAN_SYSTEM = 'taskCenterScheduleTestPlanSystem',
  TASK_SCHEDULE_TASK_TEST_PLAN_ORGANIZATION = 'taskCenterScheduleTestPlanOrganization',
  TASK_SCHEDULE_TASK_TEST_PLAN_PROJECT = 'taskCenterScheduleTestPlanProject',
  TASK_PLAN_SYSTEM = 'taskCenterPlanSystem',
  TASK_PLAN_ORGANIZATION = 'taskCenterPlanOrganization',
  TASK_PLAN_PROJECT = 'taskCenterPlanProject',
  ASSOCIATE_CASE_API = 'associateCaseApi',
  ASSOCIATE_CASE_API_CASE = 'associateCaseApiCase',
  ASSOCIATE_CASE_API_SCENARIO = 'associateCaseApiScenario',
  ASSOCIATE_CASE = 'associateCase',
}

// 具有特殊功能的列
export enum SpecialColumnEnum {
  // 系统id
  ID = 'num',
  // 名称
  NAME = 'name',
  // 状态列
  ENABLE = 'enable',
  // 操作列
  ACTION = 'action',
  // 操作列
  OPERATION = 'operation',
}

export enum SelectAllEnum {
  ALL = 'all',
  CURRENT = 'current',
  NONE = 'none',
}

// 列编辑类型
export enum ColumnEditTypeEnum {
  INPUT = 'input',
  SELECT = 'select',
  SEARCH_SELECT = 'search_select',
}

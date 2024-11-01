export enum ApiTestRouteEnum {
  API_TEST = 'apiTest',
  API_TEST_DEBUG_MANAGEMENT = 'apiTestDebug',
  API_TEST_MANAGEMENT = 'apiTestManagement',
  API_TEST_MANAGEMENT_RECYCLE = 'apiTestManagementRecycle',
  API_TEST_SCENARIO = 'apiTestScenario',
  API_TEST_SCENARIO_RECYCLE = 'apiTestScenarioRecycle',
  API_TEST_REPORT = 'apiTestReport',
  API_TEST_REPORT_SHARE = 'apiTestReportShare',
}

export enum BugManagementRouteEnum {
  BUG_MANAGEMENT = 'bugManagement',
  BUG_MANAGEMENT_INDEX = 'bugManagementIndex',
  BUG_MANAGEMENT_DETAIL = 'bugManagementIndexDetail',
  BUG_MANAGEMENT_RECYCLE = 'bugManagementIndexRecycle',
  BUG_MANAGEMENT_CREATE_SUCCESS = 'bugManagementIndexCreateSuccess',
}

export enum CaseManagementRouteEnum {
  CASE_MANAGEMENT = 'caseManagement',
  CASE_MANAGEMENT_CASE = 'caseManagementCase',
  CASE_MANAGEMENT_CASE_CREATE_SUCCESS = 'caseManagementCaseCreateSuccess',
  CASE_MANAGEMENT_CASE_RECYCLE = 'caseManagementCaseRecycle',
  CASE_MANAGEMENT_CASE_DETAIL = 'caseManagementCaseDetail',
  CASE_MANAGEMENT_REVIEW = 'caseManagementReview',
  CASE_MANAGEMENT_REVIEW_CREATE = 'caseManagementReviewCreate',
  CASE_MANAGEMENT_REVIEW_DETAIL = 'caseManagementReviewDetail',
  CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL = 'caseManagementReviewDetailCaseDetail',
}

export enum PerformanceTestRouteEnum {
  PERFORMANCE_TEST = 'loadTest',
}

export enum ProjectManagementRouteEnum {
  PROJECT_MANAGEMENT = 'projectManagement',
  PROJECT_MANAGEMENT_FILE_MANAGEMENT = 'projectManagementFileManageMent',
  PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT = 'projectManagementMessageManagement',
  PROJECT_MANAGEMENT_COMMON_SCRIPT = 'projectManagementCommonScript',
  PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_EDIT = 'projectManagementMessageManagementEdit',
  PROJECT_MANAGEMENT_TASK_CENTER = 'projectManagementTaskCenter',
  PROJECT_MANAGEMENT_LOG = 'projectManagementLog',
  PROJECT_MANAGEMENT_PERMISSION = 'projectManagementPermission',
  PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO = 'projectManagementPermissionBasicInfo',
  PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT = 'projectManagementPermissionMenuManagement',
  PROJECT_MANAGEMENT_TEMPLATE = 'projectManagementTemplate',
  PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT = 'projectManagementTemplateList',
  PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_CASE_DETAIL = 'projectManagementTemplateManagementCaseDetail',
  PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_API_DETAIL = 'projectManagementTemplateManagementApiDetail',
  PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_BUG_DETAIL = 'projectManagementTemplateManagementBugDetail',
  PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_WORKFLOW = 'projectManagementTemplateManagementWorkFlow',
  PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING = 'projectManagementTemplateFiledSetting',
  PROJECT_MANAGEMENT_PERMISSION_VERSION = 'projectManagementPermissionVersion',
  PROJECT_MANAGEMENT_PERMISSION_USER_GROUP = 'projectManagementPermissionUserGroup',
  PROJECT_MANAGEMENT_PERMISSION_MEMBER = 'projectManagementPermissionMember',
  PROJECT_MANAGEMENT_MENU_MANAGEMENT_ERROR_REPORT_RULE = 'projectManagementMenuManagementErrorReportRule',
  PROJECT_MANAGEMENT_ENVIRONMENT_MANAGEMENT = 'projectManagementEnvironmentManagement',
}

export enum TestPlanRouteEnum {
  TEST_PLAN = 'testPlan',
  TEST_PLAN_INDEX = 'testPlanIndex',
  TEST_PLAN_INDEX_DETAIL = 'testPlanIndexDetail',
  TEST_PLAN_INDEX_CONFIG = 'testPlanIndexConfig',
  TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL = 'testPlanIndexDetailFeatureCaseDetail',
  TEST_PLAN_REPORT = 'testPlanReport',
  TEST_PLAN_REPORT_DETAIL = 'testPlanReportDetail',
}

export enum UITestRouteEnum {
  UI_TEST = 'uiTest',
}

export enum WorkbenchRouteEnum {
  WORKBENCH = 'workstation',
  WORKBENCH_INDEX = 'workstationIndex',
  WORKBENCH_INDEX_WAIT = 'workstationIndexWait',
  WORKBENCH_INDEX_FOLLOW = 'workstationIndexFollowed',
  WORKBENCH_INDEX_CREATED = 'workstationIndexCreated',
}

export enum SettingRouteEnum {
  SETTING = 'setting',
  SETTING_SYSTEM = 'settingSystem',
  SETTING_SYSTEM_USER_SINGLE = 'settingSystemUser',
  SETTING_SYSTEM_USER_GROUP = 'settingSystemUserGroup',
  SETTING_SYSTEM_ORGANIZATION = 'settingSystemOrganization',
  SETTING_SYSTEM_PARAMETER = 'settingSystemParameter',
  SETTING_SYSTEM_RESOURCE_POOL = 'settingSystemResourcePool',
  SETTING_SYSTEM_RESOURCE_POOL_DETAIL = 'settingSystemResourcePoolDetail',
  SETTING_SYSTEM_AUTHORIZED_MANAGEMENT = 'settingSystemAuthorizedManagement',
  SETTING_SYSTEM_LOG = 'settingSystemLog',
  SETTING_SYSTEM_TASK_CENTER = 'settingSystemTaskCenter',
  SETTING_SYSTEM_PLUGIN_MANAGEMENT = 'settingSystemPluginManagement',
  SETTING_ORGANIZATION = 'settingOrganization',
  SETTING_ORGANIZATION_MEMBER = 'settingOrganizationMember',
  SETTING_ORGANIZATION_USER_GROUP = 'settingOrganizationUserGroup',
  SETTING_ORGANIZATION_PROJECT = 'settingOrganizationProject',
  SETTING_ORGANIZATION_TEMPLATE = 'settingOrganizationTemplate',
  SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING = 'settingOrganizationTemplateFiledSetting',
  SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT = 'settingOrganizationTemplateManagement',
  SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_CASE_DETAIL = 'settingOrganizationTemplateManagementCaseDetail',
  SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_API_DETAIL = 'settingOrganizationTemplateManagementApiDetail',
  SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_BUG_DETAIL = 'settingOrganizationTemplateManagementBugDetail',
  SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW = 'settingOrganizationTemplateWorkFlow',
  SETTING_ORGANIZATION_SERVICE = 'settingOrganizationService',
  SETTING_ORGANIZATION_LOG = 'settingOrganizationLog',
  SETTING_ORGANIZATION_TASK_CENTER = 'settingOrganizationTaskCenter',
}

export enum ShareEnum {
  SHARE = 'share',
  SHARE_REPORT_SCENARIO = 'shareReportScenario',
  SHARE_DEFINITION_API = 'shareDefinitionApi',
  SHARE_REPORT_CASE = 'shareReportCase',
  SHARE_REPORT_TEST_PLAN = 'shareReportTestPlan',
}

export enum FullPageEnum {
  FULL_PAGE = 'fullPage',
  FULL_PAGE_TEST_PLAN_EXPORT_PDF = 'fullPageTestPlanExportPDF',
  FULL_PAGE_SCENARIO_EXPORT_PDF = 'fullPageScenarioExportPDF',
  FULL_PAGE_API_CASE_EXPORT_PDF = 'fullPageApiCaseExportPDF',
}

export const RouteEnum = {
  ...ApiTestRouteEnum,
  ...SettingRouteEnum,
  ...BugManagementRouteEnum,
  ...CaseManagementRouteEnum,
  ...PerformanceTestRouteEnum,
  ...ProjectManagementRouteEnum,
  ...TestPlanRouteEnum,
  ...UITestRouteEnum,
  ...WorkbenchRouteEnum,
  ...ShareEnum,
  ...FullPageEnum,
};

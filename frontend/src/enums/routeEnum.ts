export enum ApiTestRouteEnum {
  API_TEST = 'apiTest',
}

export enum BugManagementRouteEnum {
  BUG_MANAGEMENT = 'bugManagement',
}

export enum FeatureTestRouteEnum {
  FEATURE_TEST = 'featureTest',
}

export enum PerformanceTestRouteEnum {
  PERFORMANCE_TEST = 'performanceTest',
}

export enum ProjectManagementRouteEnum {
  PROJECT_MANAGEMENT = 'projectManagement',
  PROJECT_MANAGEMENT_INDEX = 'projectManagementIndex',
  PROJECT_MANAGEMENT_LOG = 'projectManagementLog',
  PROJECT_MANAGEMENT_PERMISSION = 'projectManagementPermission',
  PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO = 'projectManagementPermissionBasicInfo',
  PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT = 'projectManagementPermissionMenuManagement',
  PROJECT_MANAGEMENT_PERMISSION_VERSION = 'projectManagementPermissionVersion',
  PROJECT_MANAGEMENT_PERMISSION_USER_GROUP = 'projectManagementPermissionUserGroup',
  PROJECT_MANAGEMENT_PERMISSION_MEMBER = 'projectManagementPermissionMember',
}

export enum TestPlanRouteEnum {
  TEST_PLAN = 'testPlan',
}

export enum UITestRouteEnum {
  UI_TEST = 'uiTest',
}

export enum WorkbenchRouteEnum {
  WORKBENCH = 'workbench',
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
  SETTING_SYSTEM_PLUGIN_MANAGEMENT = 'settingSystemPluginManagement',
  SETTING_ORGANIZATION = 'settingOrganization',
  SETTING_ORGANIZATION_MEMBER = 'settingOrganizationMember',
  SETTING_ORGANIZATION_USER_GROUP = 'settingOrganizationUserGroup',
  SETTING_ORGANIZATION_PROJECT = 'settingOrganizationProject',
  SETTING_ORGANIZATION_SERVICE = 'settingOrganizationService',
  SETTING_ORGANIZATION_LOG = 'settingOrganizationLog',
}

export const RouteEnum = {
  ...ApiTestRouteEnum,
  ...SettingRouteEnum,
  ...BugManagementRouteEnum,
  ...FeatureTestRouteEnum,
  ...PerformanceTestRouteEnum,
  ...ProjectManagementRouteEnum,
  ...TestPlanRouteEnum,
  ...UITestRouteEnum,
  ...WorkbenchRouteEnum,
};

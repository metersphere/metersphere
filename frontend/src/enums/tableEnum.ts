export enum TableOpenDetailModeEnum {
  DRAWER = 'drawer',
  NEW_WINDOW = 'new_window',
}
export enum TableModuleEnum {
  USERGROUPINDEX = 'userGroupIndex',
}

export enum TableKeyEnum {
  API_TEST = 'apiTest',
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
  FILE_MANAGEMENT_FILE = 'fileManagementFile',
  FILE_MANAGEMENT_CASE = 'fileManagementCase',
  FILE_MANAGEMENT_CASE_RECYCLE = 'fileManagementCaseRecycle',
  FILE_MANAGEMENT_VERSION = 'fileManagementVersion',
  PROJECT_MANAGEMENT_MENU_FALSE_ALERT = 'projectManagementMenuFalseAlert',
  ORGANIZATION_TEMPLATE_DEFECT_TABLE = 'organizationTemplateManagementDefect',
  CASE_MANAGEMENT_TABLE = 'caseManagement',
  CASE_MANAGEMENT_DETAIL_TABLE = 'caseManagementDetailTable',
  CASE_MANAGEMENT_ASSOCIATED_TABLE = 'caseManagementAssociatedFileTable',
  BUG_MANAGEMENT = 'bugManagement',
  CASE_MANAGEMENT_DEMAND = 'caseManagementDemand',
  CASE_MANAGEMENT_REVIEW = 'caseManagementReview',
  CASE_MANAGEMENT_REVIEW_CASE = 'caseManagementReviewCase',
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

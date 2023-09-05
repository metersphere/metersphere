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
  ORGANNATIONMEMBER = 'member',
  SYSTEM_ORGANIZATION = 'systemOrganization',
  SYSTEM_PROJECT = 'systemProject',
  SYSTEM_LOG = 'systemLog',
  PROJECT_MEMBER = 'projectMember',
  ORGANIZATION_MEMBER = 'organizationMember',
  ORGANIZATION_PROJECT = 'organizationProject',
}

// 具有特殊功能的列
export enum SpecialColumnEnum {
  // 编辑列
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

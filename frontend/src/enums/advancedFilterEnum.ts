export enum OperatorEnum {
  CONTAINS = 'CONTAINS',
  NOT_CONTAINS = 'NOT_CONTAINS',
  BELONG_TO = 'IN',
  NOT_BELONG_TO = 'NOT_IN',
  GT = 'GT',
  LT = 'LT',
  EQUAL = 'EQUALS', // 有其他地方用到
  NOT_EQUAL = 'NOT_EQUALS', // 有其他地方用到
  EMPTY = 'EMPTY', // 有其他地方用到
  NOT_EMPTY = 'NOT_EMPTY', // 有其他地方用到
  BETWEEN = 'BETWEEN',
  COUNT_LT = 'COUNT_LT',
  COUNT_GT = 'COUNT_GT',
}

export enum FilterType {
  INPUT = 'Input',
  NUMBER = 'Number',
  SELECT = 'Select',
  MEMBER = 'Member',
  DATE_PICKER = 'DatePicker',
  TAGS_INPUT = 'TagsInput',
  TREE_SELECT = 'TreeSelect',
  TEXTAREA = 'textArea',
  BOOLEAN = 'boolean',
}

export enum ViewTypeEnum {
  FUNCTIONAL_CASE = 'functional-case',
  API_DEFINITION = 'api-definition',
  API_SCENARIO = 'api-scenario',
  REVIEW_FUNCTIONAL_CASE = 'review-functional-case',
  API_CASE = 'api-case',
  API_MOCK = 'api-mock',
  PLAN_API_SCENARIO = 'plan-api-scenario',
  PLAN_API_CASE = 'plan-api-case',
  PLAN_FUNCTIONAL_CASE = 'plan-functional-case',
  BUG = 'bug',
  PLAN_BUG = 'plan-bug',
  PLAN_BUG_DRAWER = 'plan-bug-drawer',
}

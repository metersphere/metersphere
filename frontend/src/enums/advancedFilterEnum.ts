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
  RADIO = 'radio',
  CHECKBOX = 'checkbox',
  CASCADER = 'Cascader',
  JIRAKEY = 'JIRAKEY',
}

export enum ViewTypeEnum {
  FUNCTIONAL_CASE = 'functional-case',
  API_DEFINITION = 'api-definition',
  REVIEW_FUNCTIONAL_CASE = 'review-functional-case',
}

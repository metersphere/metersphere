// 模板
export const CUSTOM_FIELD_TYPE_OPTION = [
  {value: 'input',text: '输入框'},
  {value: 'textarea',text: '文本框'},
  {value: 'select',text: '单选下拉列表'},
  {value: 'multipleSelect',text: '多选下拉列表'},
  {value: 'radio',text: '单选框'},
  {value: 'checkbox',text: '多选框'},
  {value: 'member',text: '单选成员'},
  {value: 'multipleMember',text: '多选成员'},
  {value: 'data',text: '日期'},
  {value: 'int',text: '整型'},
  {value: 'float',text: '浮点型'}
];

export const CUSTOM_FIELD_SCENE_OPTION = [
  {value: 'TEST_CASE',text: '用例模板'},
  {value: 'ISSUE',text: '缺陷模板'},
];

export const CASE_TYPE_OPTION = [
  {value: 'functional',text: '功能用例'},
];

export const ISSUE_PLATFORM_OPTION = [
  {value: 'metersphere',text: 'Metersphere'},
  {value: 'jira',text: 'JIRA'},
  {value: 'zentao',text: '禅道'},
];

export const FIELD_TYPE_MAP = {
  input: '输入框',
  textarea: '文本框',
  select: '单选下拉列表',
  multipleSelect: '多选下拉列表',
  radio: '单选框',
  checkbox: '多选框',
  member: '单选成员',
  multipleMember: '多选成员',
  data: '日期',
  int: '整型',
  float: '浮点型'
};

export const SCENE_MAP = {
  ISSUE: '缺陷模板',
  TEST_CASE: '用例模板'
};

export const SYSTEM_FIELD_NAME_MAP = {
  case_status: '用例状态',
  case_maintainer: '责任人',
  case_priority: '用例等级',

  issue_creator: '创建人',
  issue_processor: '处理人',
  issue_status: '状态',
  issue_severity: '严重程度'
};

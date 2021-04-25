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
  // {value: 'Jira',text: 'JIRA'},
  // {value: 'Tapd',text: 'Tapd'},
  // {value: 'Zentao',text: '禅道'},
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
  //用例字段
  i43sf4_testCaseStatus: 'custom_field.case_status',
  i43sf4_testCaseMaintainer: 'custom_field.case_maintainer',
  i43sf4_testCasePriority: 'custom_field.case_priority',
  //缺陷字段
  i43sf4_issueCreator: 'custom_field.issue_creator',
  i43sf4_issueProcessor: 'custom_field.issue_processor',
  i43sf4_issueStatus: 'custom_field.issue_status',
  i43sf4_issueSeverity: 'custom_field.issue_severity',
}


export const ISSUE_STATUS_MAP = {
  'new': '新建',
  'closed': '已关闭',
  'resolved': '已解决',
  'active': '激活'
}

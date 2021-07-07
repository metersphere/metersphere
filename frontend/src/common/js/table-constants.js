// 模板
import i18n from "@/i18n/i18n";

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
  {value: 'Jira',text: 'JIRA'},
  {value: 'Tapd',text: 'Tapd'},
  {value: 'Zentao',text: '禅道'},
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
  用例状态: 'custom_field.case_status',
  责任人: 'custom_field.case_maintainer',
  用例等级: 'custom_field.case_priority',
  //缺陷字段
  创建人: 'custom_field.issue_creator',
  处理人: 'custom_field.issue_processor',
  状态: 'custom_field.issue_status',
  严重程度: 'custom_field.issue_severity',
}


export const ISSUE_STATUS_MAP = {
  'new': '新建',
  'closed': '已关闭',
  'resolved': '已解决',
  'active': '激活',
  'delete': '已删除'
}

export const API_SCENARIO_FILTERS = {
  LEVEL_FILTERS: [
    {text: 'P0', value: 'P0'},
    {text: 'P1', value: 'P1'},
    {text: 'P2', value: 'P2'},
    {text: 'P3', value: 'P3'}
  ],
  RESULT_FILTERS: [
    {text: 'Fail', value: 'Fail'},
    {text: 'Success', value: 'Success'}
  ],
  STATUS_FILTERS: [
    {text: i18n.t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
    {text: i18n.t('test_track.plan.plan_status_running'), value: 'Underway'},
    {text: i18n.t('test_track.plan.plan_status_completed'), value: 'Completed'},
    {text: i18n.t('test_track.plan.plan_status_trash'), value: 'Trash'},
  ],
}

export const USER_GROUP_SCOPE = {
  // todo i18n
  'SYSTEM': '系统',
  'ORGANIZATION': '组织',
  'WORKSPACE': '工作空间',
  'PROJECT': '项目'
}

export const PROJECT_GROUP_SCOPE = {
  'TRACK': '测试跟踪',
  'API': '接口测试',
  'PERFORMANCE': '性能测试'
}

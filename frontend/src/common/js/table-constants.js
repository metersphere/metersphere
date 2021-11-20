// 模板
import i18n from "@/i18n/i18n";

export const CUSTOM_FIELD_TYPE_OPTION = [
  {value: 'input',text: i18n.t('workspace.custom_filed.input')},
  {value: 'textarea',text: i18n.t('workspace.custom_filed.textarea')},
  {value: 'select',text: i18n.t('workspace.custom_filed.select')},
  {value: 'multipleSelect',text: i18n.t('workspace.custom_filed.multipleSelect')},
  {value: 'radio',text: i18n.t('workspace.custom_filed.radio')},
  {value: 'checkbox',text: i18n.t('workspace.custom_filed.checkbox')},
  {value: 'member',text: i18n.t('workspace.custom_filed.member')},
  {value: 'multipleMember',text: i18n.t('workspace.custom_filed.multipleMember')},
  {value: 'data',text: i18n.t('workspace.custom_filed.data')},
  {value: 'int',text: i18n.t('workspace.custom_filed.int')},
  {value: 'float',text: i18n.t('workspace.custom_filed.float')},
  {value: 'multipleInput',text: i18n.t('workspace.custom_filed.multipleInput')}
];

export const CUSTOM_FIELD_SCENE_OPTION = [
  {value: 'TEST_CASE',text: i18n.t('workspace.case_template_manage')},
  {value: 'ISSUE',text: i18n.t('workspace.issue_template_manage')},
];

export const CASE_TYPE_OPTION = [
  {value: 'functional',text: '功能用例'},
];

export const ISSUE_PLATFORM_OPTION = [
  {value: 'Local',text: 'Metersphere'},
  {value: 'Jira',text: 'JIRA'},
  {value: 'Tapd',text: 'Tapd'},
  {value: 'Zentao',text: '禅道'},
  {value: 'AzureDevops',text: 'Azure Devops'},
];

export const FIELD_TYPE_MAP = {
  input: 'workspace.custom_filed.input',
  textarea: 'workspace.custom_filed.textarea',
  select: 'workspace.custom_filed.select',
  multipleSelect: 'workspace.custom_filed.multipleSelect',
  radio: 'workspace.custom_filed.radio',
  checkbox: 'workspace.custom_filed.checkbox',
  member: 'workspace.custom_filed.member',
  multipleMember: 'workspace.custom_filed.multipleMember',
  data: 'workspace.custom_filed.data',
  int: 'workspace.custom_filed.int',
  float: 'workspace.custom_filed.float',
  multipleInput: 'workspace.custom_filed.multipleInput'
};

export const SCENE_MAP = {
  ISSUE: 'workspace.issue_template_manage',
  TEST_CASE: 'workspace.case_template_manage',
  PLAN: 'workstation.table_name.track_plan'
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
  // 测试计划
  测试阶段: 'test_track.plan.plan_stage'
}


export const ISSUE_STATUS_MAP = {
  'new': '新建',
  'closed': '已关闭',
  'resolved': '已解决',
  'active': '激活',
  'delete': '已删除',
  'created':'新建'
}

export const API_SCENARIO_FILTERS = {
  LEVEL_FILTERS: [
    {text: 'P0', value: 'P0'},
    {text: 'P1', value: 'P1'},
    {text: 'P2', value: 'P2'},
    {text: 'P3', value: 'P3'}
  ],
  RESULT_FILTERS: [
    {text: i18n.t('api_test.automation.fail'), value: 'Fail'},
    {text: i18n.t('api_test.automation.success'), value: 'Success'}
  ],
  STATUS_FILTERS: [
    {text: i18n.t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
    {text: i18n.t('test_track.plan.plan_status_running'), value: 'Underway'},
    {text: i18n.t('test_track.plan.plan_status_completed'), value: 'Completed'},
    {text: i18n.t('test_track.plan.plan_status_trash'), value: 'Trash'},
  ],
}

export const USER_GROUP_SCOPE = {
  'SYSTEM': 'group.system',
  'WORKSPACE': 'group.workspace',
  'PROJECT': 'group.project'
}

export const PROJECT_GROUP_SCOPE = {
  'TRACK': '测试跟踪',
  'API': '接口测试',
  'PERFORMANCE': '性能测试',
  'REPORT': '报表统计'
}

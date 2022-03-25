// 模板
import i18n from "@/i18n/i18n";
import {AZURE_DEVOPS, JIRA, LOCAL, TAPD, ZEN_TAO} from "@/common/js/constants";

export const CUSTOM_FIELD_TYPE_OPTION = [
  {value: 'input',text: 'workspace.custom_filed.input'},
  {value: 'textarea',text: 'workspace.custom_filed.textarea'},
  {value: 'select',text: 'workspace.custom_filed.select'},
  {value: 'multipleSelect',text: 'workspace.custom_filed.multipleSelect'},
  {value: 'radio',text: 'workspace.custom_filed.radio'},
  {value: 'checkbox',text: 'workspace.custom_filed.checkbox'},
  {value: 'member',text: 'workspace.custom_filed.member'},
  {value: 'multipleMember',text: 'workspace.custom_filed.multipleMember'},
  {value: 'date',text: 'workspace.custom_filed.date'},
  {value: 'datetime',text: 'workspace.custom_filed.datetime'},
  {value: 'richText',text: 'workspace.custom_filed.richText'},
  {value: 'int',text: 'workspace.custom_filed.int'},
  {value: 'float',text: 'workspace.custom_filed.float'},
  {value: 'multipleInput',text: 'workspace.custom_filed.multipleInput'}
];

export const CUSTOM_FIELD_SCENE_OPTION = [
  {value: 'TEST_CASE',text: 'workspace.case_template_manage'},
  {value: 'ISSUE',text: 'workspace.issue_template_manage'},
];

export function CASE_TYPE_OPTION(){
  return [
    {value: 'functional', text: i18n.t('api_test.home_page.failed_case_list.table_value.case_type.functional')},
  ];
}

export const ISSUE_PLATFORM_OPTION = [
  {value: LOCAL, text: 'Metersphere'},
  {value: TAPD, text: 'Tapd'},
  {value: JIRA, text: 'JIRA'},
  {value: ZEN_TAO, text: 'Zentao'},
  {value: AZURE_DEVOPS, text: 'Azure Devops'},
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
  date: 'workspace.custom_filed.date',
  datetime: 'workspace.custom_filed.datetime',
  richText: 'workspace.custom_filed.richText',
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
  'new': i18n.t('test_track.issue.status_new'),
  'closed': i18n.t('test_track.issue.status_closed'),
  'resolved': i18n.t('test_track.issue.status_resolved'),
  'active': i18n.t('test_track.issue.status_active'),
  'delete': i18n.t('test_track.issue.status_delete'),
  'created':i18n.t('test_track.issue.status_new')
}

export function API_SCENARIO_FILTERS () {
  return {
    STATUS_FILTERS: [
      {text: i18n.t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
      {text: i18n.t('test_track.plan.plan_status_running'), value: 'Underway'},
      {text: i18n.t('test_track.plan.plan_status_completed'), value: 'Completed'},
      {text: i18n.t('test_track.plan.plan_status_trash'), value: 'Trash'},
    ],
    LEVEL_FILTERS: [
      {text: 'P0', value: 'P0'},
      {text: 'P1', value: 'P1'},
      {text: 'P2', value: 'P2'},
      {text: 'P3', value: 'P3'}
    ],
    RESULT_FILTERS: [
      {text: i18n.t('api_test.automation.fail'), value: 'Fail'},
      {text: i18n.t('api_test.automation.success'), value: 'Success'}
    ]
  };
  /*LEVEL_FILTERS: [
    {text: 'P0', value: 'P0'},
    {text: 'P1', value: 'P1'},
    {text: 'P2', value: 'P2'},
    {text: 'P3', value: 'P3'}
  ];
  RESULT_FILTERS: [
    {text: i18n.t('api_test.automation.fail'), value: 'Fail'},
    {text: i18n.t('api_test.automation.success'), value: 'Success'}
  ];*/

  /*STATUS_FILTERS: [
    {text: i18n.t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
    {text: i18n.t('test_track.plan.plan_status_running'), value: 'Underway'},
    {text: i18n.t('test_track.plan.plan_status_completed'), value: 'Completed'},
    {text: i18n.t('test_track.plan.plan_status_trash'), value: 'Trash'},
  ],*/
}



export const USER_GROUP_SCOPE = {
  'SYSTEM': 'group.system',
  'WORKSPACE': 'group.workspace',
  'PROJECT': 'group.project',
  'PERSONAL': 'group.personal'
}

export const PROJECT_GROUP_SCOPE = {
  'TRACK': 'permission.other.track',
  'API': 'permission.other.api',
  'PERFORMANCE': 'permission.other.performance',
  'REPORT': 'permission.other.report'
}

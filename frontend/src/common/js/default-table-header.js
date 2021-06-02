import i18n from "@/i18n/i18n";

export const TEST_CASE_LIST = 'test_case_list';

export const CUSTOM_FIELD_LIST = new Set([
  'name',
  'scene',
  'type',
  'remark',
  'system',
  'createTime',
  'updateTime'
]);

export const TEST_CASE_TEMPLATE_LIST = new Set([
  'name',
  'type',
  'description',
  'createTime',
  'updateTime'
]);

export const ISSUE_TEMPLATE_LIST = new Set([
  'name',
  'platform',
  'description',
  'createTime',
  'updateTime'
]);

export const CUSTOM_TABLE_HEADER = {

  CUSTOM_FIELD: [
    {id: 'name', key: '1', label: i18n.t('commons.name')},
    {id: 'scene', key: '2', label: i18n.t('custom_field.scene')},
    {id: 'type', key: '3', label: i18n.t('custom_field.attribute_type')},
    {id: 'remark', key: '4', label: i18n.t('commons.remark')},
    {id: 'system', key: '5', label: i18n.t('custom_field.system_field')},
    {id: 'createTime', key: '6', label: i18n.t('commons.create_time')},
    {id: 'updateTime', key: '7', label: i18n.t('commons.update_time')},
  ],

  API_DEFINITION: [
    {id: 'num', key: '1', label: "ID"},
    {id: 'name', key: '2', label: i18n.t('api_test.definition.api_name')},
    {id: 'method', key: '3', label: i18n.t('api_test.definition.api_type')},
    {id: 'userName', key: '4', label: i18n.t('api_test.definition.api_principal')},
    {id: 'path', key: '5', label: i18n.t('api_test.definition.api_path')},
    {id: 'tags', key: '6', label: i18n.t('commons.tag')},
    {id: 'updateTime', key: '7', label: i18n.t('api_test.definition.api_last_time')},
    {id: 'caseTotal', key: '8', label: i18n.t('api_test.definition.api_case_number')},
    {id: 'caseStatus', key: '9', label: i18n.t('api_test.definition.api_case_status')},
    {id: 'casePassingRate', key: 'a', label: i18n.t('api_test.definition.api_case_passing_rate')},
    {id: 'status', key: 'b', label: i18n.t('api_test.definition.api_status')}
  ],

  API_CASE: [
    {id: 'num', key: '1', label: "ID"},
    {id: 'name', key: '2', label: i18n.t('test_track.case.name')},
    {id: 'priority', key: '3', label: i18n.t('test_track.case.priority')},
    {id: 'path', key: '4', label: 'API'+i18n.t('api_test.definition.api_path')},
    {id: 'casePath', key: '5', label: i18n.t('api_test.definition.request.case')+i18n.t('api_test.definition.api_path')},
    {id: 'tags', key: '6', label: i18n.t('commons.tag')},
    {id: 'createUser', key: '7', label: "创建人"},
    {id: 'updateTime', key: '8', label: i18n.t('api_test.definition.api_last_time')},
  ],

  API_SCENARIO: [
    {id: 'num', key: '1', label: "ID"},
    {id: 'name',key: '2',  label: i18n.t('api_report.scenario_name')},
    {id: 'level', key: '3', label: i18n.t('api_test.automation.case_level')},
    {id: 'status', key: '4', label: i18n.t('test_track.plan.plan_status')},
    {id: 'tags', key: '5', label: i18n.t('commons.tag')},
    {id: 'userId', key: '6', label: i18n.t('api_test.automation.creator')},
    {id: 'principal', key: '7', label: i18n.t('api_test.definition.api_principal')},
    {id: 'updateTime', key: '8', label: i18n.t('api_test.definition.api_last_time')},
    {id: 'stepTotal', key: '9', label: i18n.t('api_test.automation.step')},
    {id: 'lastResult', key: 'a', label: i18n.t('api_test.automation.last_result')},
    {id: 'passRate', key: 'b', label: i18n.t('api_test.automation.passing_rate')},
  ]

}


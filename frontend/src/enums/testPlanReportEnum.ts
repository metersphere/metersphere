export enum ReportCardTypeEnum {
  SUMMARY = 'SUMMARY', // 报告总结
  BUG_DETAIL = 'BUG_DETAIL', // 缺陷明细
  FUNCTIONAL_DETAIL = 'FUNCTIONAL_DETAIL', // 功能用例明细
  API_CASE_DETAIL = 'API_CASE_DETAIL', // 接口用例明细
  SCENARIO_CASE_DETAIL = 'SCENARIO_CASE_DETAIL', // 场景用例明细
  SUB_PLAN_DETAIL = 'SUB_PLAN_DETAIL', // 计划组子计划详情
  CUSTOM_CARD = 'CUSTOM_CARD', // 自定义卡片
}
export enum FieldTypeEnum {
  SYSTEM = 'SYSTEM',
  RICH_TEXT = 'RICH_TEXT',
}

export default {};

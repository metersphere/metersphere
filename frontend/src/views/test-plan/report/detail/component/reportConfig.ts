import { defaultCount } from '@/config/testPlan';

import { ApiOrScenarioCaseItem, FeatureCaseItem, ReportBugItem } from '@/models/testPlan/report';
import type { configItem } from '@/models/testPlan/testPlanReport';
import { PlanReportDetail } from '@/models/testPlan/testPlanReport';
import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

export const commonDefaultConfig: configItem[] = [
  {
    id: ReportCardTypeEnum.SUMMARY,
    value: ReportCardTypeEnum.SUMMARY,
    label: 'report.detail.reportSummary',
    system: true,
    enableEdit: false,
  },
  {
    id: ReportCardTypeEnum.BUG_DETAIL,
    value: ReportCardTypeEnum.BUG_DETAIL,
    label: 'report.detail.bugDetails',
    system: true,
    enableEdit: false,
  },
  {
    id: ReportCardTypeEnum.FUNCTIONAL_DETAIL,
    value: ReportCardTypeEnum.FUNCTIONAL_DETAIL,
    label: 'report.detail.featureCaseDetails',
    system: true,
    enableEdit: false,
  },
  {
    id: ReportCardTypeEnum.API_CASE_DETAIL,
    value: ReportCardTypeEnum.API_CASE_DETAIL,
    label: 'report.detail.apiCaseDetails',
    system: true,
    enableEdit: false,
  },
  {
    id: ReportCardTypeEnum.SCENARIO_CASE_DETAIL,
    value: ReportCardTypeEnum.SCENARIO_CASE_DETAIL,
    label: 'report.detail.scenarioCaseDetails',
    system: true,
    enableEdit: false,
  },
];

export const defaultCustomConfig: configItem = {
  id: '',
  value: ReportCardTypeEnum.CUSTOM_CARD,
  label: 'report.detail.customDefaultCardName',
  system: false,
  enableEdit: false,
  content: '',
};

// 独立报告默认配置
export const defaultSingleConfig: configItem[] = [...commonDefaultConfig];
// 集合报告默认配置
export const defaultGroupConfig: configItem[] = [
  {
    id: ReportCardTypeEnum.SUB_PLAN_DETAIL,
    value: ReportCardTypeEnum.SUB_PLAN_DETAIL,
    label: 'report.detail.subPlanDetails',
    system: true,
    enableEdit: false,
  },
  ...commonDefaultConfig,
];

interface NamedItem {
  name?: string;
  title?: string;
  testPlanName?: string;
}

export function createData<T extends NamedItem>(listItem: T): T[] {
  const list = [];
  for (let index = 0; index < 10; index++) {
    const numIndex = index + 1;
    const item = {
      id: `Example_${index}`,
      ...listItem,
      num: numIndex,
      name: `${listItem.name}_${numIndex}`,
      title: `${listItem.title}_${numIndex}`,
      testPlanName: `${listItem.testPlanName}_${numIndex}`,
    };
    list.push(item);
  }
  return list;
}

// 示例数据
// 子计划报告
const subPlanList: PlanReportDetail = {
  id: 'Example_738373617320062',
  name: '子计划明细_示例数据',
  testPlanName: '子计划明细_示例数据',
  createTime: 1719374179322,
  startTime: 0,
  endTime: 0,
  summary: '',
  caseTotal: 1,
  passThreshold: 100.0,
  passRate: 0.0,
  executeRate: 0,
  bugCount: 0,
  planCount: 0,
  executeCount: defaultCount,
  functionalCount: defaultCount,
  apiCaseCount: defaultCount,
  apiScenarioCount: defaultCount,
  passCountOfPlan: 0,
  failCountOfPlan: 0,
  functionalBugCount: 0,
  apiBugCount: 0,
  scenarioBugCount: 0,
  resultStatus: 'SUCCESS',
};

// 功能用例明细
const functionalList: FeatureCaseItem = {
  id: 'Example_738373617320062',
  num: 1,
  name: '用例明细_示例数据',
  moduleName: '/未规划模块',
  priority: 'P1',
  executeResult: 'SUCCESS',
  executeUserName: '',
  bugCount: 0,
  reportId: '',
};
// 缺陷明细
const bugList: ReportBugItem = {
  id: 'Example_738373617320062',
  num: 1,
  title: '缺陷明细_示例数据',
  status: '新建',
  handleUserName: 'admin',
  relationCaseCount: 0,
};
// 接口明细
const apiCaseList: ApiOrScenarioCaseItem = {
  id: 'Example_738373617320062',
  num: 1,
  name: '接口明细_示例数据',
  moduleName: '/未规划模块',
  priority: 'P0',
  executeResult: 'SUCCESS',
  executeUser: 'admin',
  bugCount: 0,
  reportId: '718255970852864',
  projectId: '718255970852864',
};
// 场景明细
const scenarioCaseList: ApiOrScenarioCaseItem = {
  id: 'Example_738373617320062',
  num: 1,
  name: '场景明细_示例数据',
  moduleName: '/未规划模块',
  priority: 'P2',
  executeResult: 'SUCCESS',
  executeUser: '社恐程序员',
  bugCount: 0,
  reportId: '718255970852864',
  projectId: '718255970852864',
};

export const detailTableExample: Record<string, any> = {
  [ReportCardTypeEnum.SUB_PLAN_DETAIL]: createData<PlanReportDetail>(subPlanList),
  [ReportCardTypeEnum.FUNCTIONAL_DETAIL]: createData<FeatureCaseItem>(functionalList),
  [ReportCardTypeEnum.BUG_DETAIL]: createData<ReportBugItem>(bugList),
  [ReportCardTypeEnum.API_CASE_DETAIL]: createData<ApiOrScenarioCaseItem>(apiCaseList),
  [ReportCardTypeEnum.SCENARIO_CASE_DETAIL]: createData<ApiOrScenarioCaseItem>(scenarioCaseList),
};

export default {};

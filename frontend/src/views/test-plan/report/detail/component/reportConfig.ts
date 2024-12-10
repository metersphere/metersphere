import type { MsTableColumn } from '@/components/pure/ms-table/type';

import { defaultCount } from '@/config/testPlan';

import { ApiOrScenarioCaseItem, ReportBugItem } from '@/models/testPlan/report';
import type { configItem } from '@/models/testPlan/testPlanReport';
import { PlanReportDetail } from '@/models/testPlan/testPlanReport';
import { FieldTypeEnum, ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

const summaryConfig: configItem[] = [
  {
    id: ReportCardTypeEnum.SUMMARY,
    value: ReportCardTypeEnum.SUMMARY,
    label: 'report.detail.reportSummary',
    type: FieldTypeEnum.SYSTEM,
    enableEdit: false,
  },
];
const subPlanConfig: configItem[] = [
  {
    id: ReportCardTypeEnum.SUB_PLAN_DETAIL,
    value: ReportCardTypeEnum.SUB_PLAN_DETAIL,
    label: 'report.detail.subPlanDetails',
    type: FieldTypeEnum.SYSTEM,
    enableEdit: false,
  },
];

export const commonDefaultConfig: configItem[] = [
  ...summaryConfig,
  {
    id: ReportCardTypeEnum.BUG_DETAIL,
    value: ReportCardTypeEnum.BUG_DETAIL,
    label: 'report.detail.bugDetails',
    type: FieldTypeEnum.SYSTEM,
    enableEdit: false,
  },
  {
    id: ReportCardTypeEnum.FUNCTIONAL_DETAIL,
    value: ReportCardTypeEnum.FUNCTIONAL_DETAIL,
    label: 'report.detail.featureCaseDetails',
    type: FieldTypeEnum.SYSTEM,
    enableEdit: false,
  },
  {
    id: ReportCardTypeEnum.API_CASE_DETAIL,
    value: ReportCardTypeEnum.API_CASE_DETAIL,
    label: 'report.detail.apiCaseDetails',
    type: FieldTypeEnum.SYSTEM,
    enableEdit: false,
  },
  {
    id: ReportCardTypeEnum.SCENARIO_CASE_DETAIL,
    value: ReportCardTypeEnum.SCENARIO_CASE_DETAIL,
    label: 'report.detail.scenarioCaseDetails',
    type: FieldTypeEnum.SYSTEM,
    enableEdit: false,
  },
];

export const defaultCustomConfig: configItem = {
  id: '',
  value: ReportCardTypeEnum.CUSTOM_CARD,
  label: 'report.detail.customDefaultCardName',
  type: FieldTypeEnum.RICH_TEXT,
  enableEdit: false,
  content: '',
};

// 独立报告默认选择列表配置
export const defaultSingleConfig: configItem[] = [...commonDefaultConfig];
// 集合报告默认选择列表配置
export const defaultGroupConfig: configItem[] = [...subPlanConfig, ...commonDefaultConfig];
// 集合报告默认展示卡片列表配置
export const defaultGroupCardConfig: configItem[] = [...subPlanConfig, ...summaryConfig];

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
  functionalTotal: 0,
  apiCaseTotal: 0,
  apiScenarioTotal: 0,
  passThreshold: 100,
  passRate: 100,
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
  defaultLayout: true,
};

// 功能用例明细
const functionalList: ApiOrScenarioCaseItem = {
  id: 'Example_738373617320062',
  num: 1,
  name: '用例明细_示例数据',
  moduleName: '/未规划模块',
  priority: 'P1',
  executeResult: 'SUCCESS',
  executeUser: 'admin',
  bugCount: 0,
  reportId: '',
  projectId: '',
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
  requestTime: 0,
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
  requestTime: 0,
  executeUser: '社恐程序员',
  bugCount: 0,
  reportId: '718255970852864',
  projectId: '718255970852864',
};

export const detailTableExample: Record<string, any> = {
  [ReportCardTypeEnum.SUB_PLAN_DETAIL]: createData<PlanReportDetail>(subPlanList),
  [ReportCardTypeEnum.FUNCTIONAL_DETAIL]: createData<ApiOrScenarioCaseItem>(functionalList),
  [ReportCardTypeEnum.BUG_DETAIL]: createData<ReportBugItem>(bugList),
  [ReportCardTypeEnum.API_CASE_DETAIL]: createData<ApiOrScenarioCaseItem>(apiCaseList),
  [ReportCardTypeEnum.SCENARIO_CASE_DETAIL]: createData<ApiOrScenarioCaseItem>(scenarioCaseList),
};

export const iconTypeStatus: Record<string, any> = {
  SUCCESS: {
    icon: 'icon-icon_succeed_colorful',
    label: 'common.success',
  },
  ERROR: {
    icon: 'icon-icon_close_colorful',
    label: 'common.fail',
  },
  DEFAULT: {
    label: '-',
    color: '!text-[var(--color-text-input-border)]',
  },
};

export const testPlanNameColumn: MsTableColumn = [
  {
    title: 'report.plan.name',
    dataIndex: 'planName',
    sortIndex: 0,
    showTooltip: true,
    width: 200,
    showInTable: true,
    showDrag: false,
    columnSelectorDisabled: true,
  },
];
export const lastStaticColumns: MsTableColumn = [
  {
    title: 'common.belongModule',
    dataIndex: 'moduleName',
    showTooltip: true,
    width: 200,
    showInTable: true,
    showDrag: true,
  },
  {
    title: 'testPlan.featureCase.executor',
    dataIndex: 'executeUser',
    showTooltip: true,
    showInTable: true,
    showDrag: true,
    width: 150,
  },
  {
    title: 'testPlan.featureCase.bugCount',
    dataIndex: 'bugCount',
    showInTable: true,
    showDrag: true,
    width: 100,
  },
];
export const collectionNameColumn: MsTableColumn = [
  {
    title: 'ms.case.associate.testSet',
    dataIndex: 'collectionName',
    sortIndex: 0,
    showInTable: true,
    showDrag: false,
    width: 200,
    columnSelectorDisabled: true,
  },
];

import { TriggerModeLabelEnum } from '@/enums/reportEnum';
import { FieldTypeEnum, ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

export interface countDetail {
  success: number;
  error: number;
  fakeError: number;
  block: number;
  pending: number;
}
export interface PlanReportDetail {
  id: string;
  name: string;
  startTime: number; // 报告执行开始时间
  createTime: number; // 报告生成时间
  endTime: number; // 报告执行结束时间
  summary: string;
  passThreshold: number; // 通过阈值
  passRate: number; // 通过率
  executeRate: number; // 执行完成率
  bugCount: number;
  caseTotal: number;
  functionalTotal: number;
  apiCaseTotal: number;
  apiScenarioTotal: number;
  executeCount: countDetail;
  functionalCount: countDetail;
  apiCaseCount: countDetail; // 接口场景用例分析-用例数
  apiScenarioCount: countDetail; // 接口场景用例分析-用例数
  planCount: number;
  passCountOfPlan: number; // 计划通过数量
  failCountOfPlan: number; // 计划未通过数量
  functionalBugCount: number; // 用例明细bug总数
  apiBugCount: number; // 接口用例明细bug总数
  scenarioBugCount: number; // 场景用例明细bug总数
  testPlanName: string;
  resultStatus?: string; // 报告结果
  defaultLayout: boolean; // 报告布局
  children?: PlanReportDetail[]; // 计划组子计划数据
}

export type detailCountKey = 'functionalCount' | 'apiCaseCount' | 'apiScenarioCount';

export type AnalysisType = 'FUNCTIONAL' | 'API' | 'SCENARIO';

export interface ReportMetricsItemModel {
  unit: string;
  value: number | string;
  name: string;
  icon: string;
  tip?: string;
  runMode?: string;
}

export interface StatusListType {
  label: string;
  value: keyof countDetail;
  color: string;
  class: string;
  rateKey: string;
  key: string;
}

export interface configItem {
  id: string;
  value: ReportCardTypeEnum;
  label: string;
  content?: string;
  type: FieldTypeEnum;
  enableEdit: boolean;
  richTextTmpFileIds?: string[];
}

export interface customValueForm {
  content?: string;
  label: string;
  richTextTmpFileIds?: string[];
}

export interface componentItem {
  name: ReportCardTypeEnum; // 组件名称
  label: string; // 组件标题
  type: FieldTypeEnum; // 组件分类
  value?: string; // 组件内容
  pos: number;
}

// 手动生成报告
export interface manualReportGenParams {
  projectId: string;
  testPlanId: string;
  triggerMode: TriggerModeLabelEnum; // 触发方式
  reportName: string;
  components: componentItem[]; // 自定义组件
  richTextTmpFileIds?: string[];
}

export type SelectedReportCardTypes =
  | ReportCardTypeEnum.FUNCTIONAL_DETAIL
  | ReportCardTypeEnum.API_CASE_DETAIL
  | ReportCardTypeEnum.SCENARIO_CASE_DETAIL;

// 测试集列表
export interface testPlanSetItem {
  id: string;
  name: string; // 测试集名称
  count: number;
  planName: string; // 计划名称
}

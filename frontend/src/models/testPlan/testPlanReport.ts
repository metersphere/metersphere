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
}

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

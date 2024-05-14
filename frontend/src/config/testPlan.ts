import type { PassRateCountDetail, planStatusType, TestPlanDetail } from '@/models/testPlan/testPlan';

// TODO: 对照后端字段
// 测试计划详情
export const testPlanDefaultDetail: TestPlanDetail = {
  id: '',
  name: '',
  moduleId: '',
  tags: [],
  testPlanning: false,
  automaticStatusUpdate: true,
  repeatCase: false,
  passThreshold: 100,
  num: 0,
  status: 'PREPARED' as planStatusType,
  followFlag: false,
  passRate: 0,
  executedCount: 0,
  caseCount: 0,
  passCount: 0,
  unPassCount: 0,
  reReviewedCount: 0,
  underReviewedCount: 0,
};

export const initDetailCount: PassRateCountDetail = {
  id: '',
  passThreshold: 0,
  passRate: 0,
  executeRate: 0,
  successCount: 0,
  errorCount: 0,
  fakeErrorCount: 0,
  blockCount: 0,
  pendingCount: 0,
  caseTotal: 0,
  functionalCaseCount: 0,
  apiCaseCount: 0,
  apiScenarioCount: 0,
};

export default {};

import { ReviewStatus } from '@/models/caseManagement/caseReview';
import type { TestPlanDetail } from '@/models/testPlan/testPlan';

// TODO: 对照后端
// 测试计划详情
export const testPlanDefaultDetail: TestPlanDetail = {
  id: '',
  name: '',
  num: 0,
  status: 'PREPARED' as ReviewStatus,
  followFlag: false,
  passRate: 0,
  executedCount: 0,
  caseCount: 0,
  passCount: 0,
  unPassCount: 0,
  reReviewedCount: 0,
  underReviewedCount: 0,
};

export default {};

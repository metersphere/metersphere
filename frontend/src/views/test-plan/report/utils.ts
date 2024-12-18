import type { countDetail } from '@/models/testPlan/testPlanReport';

export function getSummaryDetail(detailCount: countDetail) {
  if (detailCount) {
    const { success, error, fakeError, pending, block } = detailCount;
    // 已执行用例
    const hasExecutedCase = success + error + fakeError + block;
    // 用例总数
    const caseTotal = hasExecutedCase + pending;
    // 执行率
    const executedCount = (hasExecutedCase / caseTotal) * 100;
    const apiExecutedRate = `${Number.isNaN(executedCount) ? 0 : executedCount.toFixed(2)}%`;
    // 通过率
    const successCount = (success / caseTotal) * 100;
    const successRate = `${Number.isNaN(successCount) ? 0 : successCount.toFixed(2)}%`;
    return {
      hasExecutedCase,
      caseTotal,
      apiExecutedRate,
      successRate,
      pending,
      success,
    };
  }
  return {
    hasExecutedCase: 0,
    caseTotal: 0,
    apiExecutedRate: 0,
    successRate: 0,
    pending: 0,
    success: 0,
  };
}

export default {};

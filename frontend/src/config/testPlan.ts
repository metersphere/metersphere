import { cloneDeep } from 'lodash-es';

import { addCommasToNumber } from '@/utils';

import type { PassRateCountDetail, planStatusType, TestPlanDetail } from '@/models/testPlan/testPlan';
import type { countDetail, PlanReportDetail, StatusListType } from '@/models/testPlan/testPlanReport';
import { LastExecuteResults } from '@/enums/caseEnum';

import type { TooltipOption } from 'echarts/types/dist/shared';
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

export const defaultDetailCount: PassRateCountDetail = {
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
  scheduleConfig: {
    resourceId: '',
    enable: false,
    cron: '',
    runConfig: {
      runMode: 'SERIAL',
    },
  },
  nextTriggerTime: 0,
  status: 'PREPARED',
  pass: false,
};

export const defaultExecuteForm = {
  lastExecResult: LastExecuteResults.SUCCESS,
  content: '',
  planCommentFileIds: [],
  notifier: [] as string[],
};

export const defaultCount: countDetail = {
  success: 0,
  error: 0,
  fakeError: 0,
  block: 0,
  pending: 0,
};
// 报告详情
export const defaultReportDetail: PlanReportDetail = {
  id: '',
  name: '',
  startTime: 0,
  createTime: 0, // 报告执行开始时间
  endTime: 0,
  summary: '',
  passThreshold: 0, // 通过阈值
  passRate: 0, // 通过率
  executeRate: 0, // 执行完成率
  bugCount: 0,
  caseTotal: 0,
  functionalTotal: 0,
  apiCaseTotal: 0,
  apiScenarioTotal: 0,
  executeCount: cloneDeep(defaultCount),
  functionalCount: cloneDeep(defaultCount),
  apiCaseCount: cloneDeep(defaultCount),
  apiScenarioCount: cloneDeep(defaultCount),
  planCount: 0,
  passCountOfPlan: 0, // 计划通过数量
  failCountOfPlan: 0, // 计划未通过数量
  functionalBugCount: 0, // 用例明细bug总数
  apiBugCount: 0, // 接口用例明细bug总数
  scenarioBugCount: 0, // 场景用例明细bug总数
  testPlanName: '',
  defaultLayout: true, // 是否是默认布局
};

export const statusConfig: StatusListType[] = [
  {
    label: 'common.unExecute',
    value: 'pending',
    color: '#D4D4D8',
    class: 'bg-[var(--color-text-input-border)]',
    rateKey: 'requestPendingRate',
    key: 'PENDING',
  },
  {
    label: 'common.success',
    value: 'success',
    color: '#00C261',
    class: 'bg-[rgb(var(--success-6))]',
    rateKey: 'requestPassRate',
    key: 'SUCCESS',
  },
  // TODO 这个版本不展示误报
  {
    label: 'common.fakeError',
    value: 'fakeError',
    color: '#FFC14E',
    class: 'bg-[rgb(var(--warning-6))]',
    rateKey: 'requestFakeErrorRate',
    key: 'FAKE_ERROR',
  },
  {
    label: 'common.block',
    value: 'block',
    color: '#B379C8',
    class: 'bg-[var(--color-fill-p-3)]',
    rateKey: 'requestPendingRate',
    key: 'BLOCK',
  },
  {
    label: 'common.fail',
    value: 'error',
    color: '#ED0303',
    class: 'bg-[rgb(var(--danger-6))]',
    rateKey: 'requestErrorRate',
    key: 'ERROR',
  },
];

export const toolTipConfig: TooltipOption = {
  show: true,
  trigger: 'item',
  textStyle: {
    color: '#959598',
  },
  position: 'top',
  backgroundColor: 'transparent',
  padding: 0,
  borderWidth: 0,
  appendTo: 'body',
  formatter(params: any) {
    const html = `
      <div class="p-[16px] w-[180px] rounded flex items-center justify-between bg-[var(--color-bg-5)]">
          <div class=" flex items-center">
            <div class="mb-[2px] mr-[8px] h-[6px] w-[6px] rounded-full bg-[${params.color}]" style="background:${
      params.color
    }">
          </div>
          <div  class="text-[var(--color-text-2)]">${params.name}</div>
          </div>
          <div class="text-[var(--color-text-1)] font-medium">${addCommasToNumber(params.value)}</div>
      </div>
      `;
    return html;
  },
};

export const commonConfig = {
  tooltip: {
    show: false,
    trigger: 'item',
  },
  legend: {
    show: false,
  },
  grid: {
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
  },
};

export const seriesConfig = {
  name: '',
  type: 'pie',
  radius: ['75%', '93%'],
  center: ['50%', '50%'],
  minAngle: 5, // 设置扇区的最小角度
  minShowLabelAngle: 10, // 设置标签显示的最小角度
  avoidLabelOverlap: true, // 避免标签重叠
  label: {
    show: false,
    position: 'center',
  },
  emphasis: {
    label: {
      show: false,
      fontSize: 40,
      fontWeight: 'bold',
    },
  },
  labelLine: {
    show: false,
  },
};

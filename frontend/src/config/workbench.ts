import { cloneDeep } from 'lodash-es';

import getVisualThemeColor from '@/config/chartTheme';
import { commonConfig, toolTipConfig } from '@/config/testPlan';

import type { ModuleCardItem } from '@/models/workbench/homePage';
import { RequestDefinitionStatus } from '@/enums/apiEnum';
import { LastReviewResult } from '@/enums/caseEnum';
import { ExecuteResultEnum } from '@/enums/taskCenter';
import { TestPlanStatusEnum } from '@/enums/testPlanEnum';
import { WorkCardEnum, WorkNavValueEnum, WorkOverviewEnum, WorkOverviewIconEnum } from '@/enums/workbenchEnum';

export const contentTabList: ModuleCardItem[] = [
  {
    label: 'workbench.homePage.functionalUseCase',
    value: WorkOverviewEnum.FUNCTIONAL,
    icon: WorkOverviewIconEnum.FUNCTIONAL,
    color: 'rgb(var(--primary-5))',
    count: 0,
  },
  {
    label: 'workbench.homePage.useCaseReview',
    value: WorkOverviewEnum.CASE_REVIEW,
    icon: WorkOverviewIconEnum.CASE_REVIEW,
    color: 'rgb(var(--success-6))',
    count: 0,
  },
  {
    label: 'workbench.homePage.interfaceAPI',
    value: WorkOverviewEnum.API,
    icon: WorkOverviewIconEnum.API,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: 'workbench.homePage.interfaceCASE',
    value: WorkOverviewEnum.API_CASE,
    icon: WorkOverviewIconEnum.API_CASE,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: 'workbench.homePage.interfaceScenario',
    value: WorkOverviewEnum.API_SCENARIO,
    icon: WorkOverviewIconEnum.API_SCENARIO,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: 'workbench.homePage.testPlan',
    value: WorkOverviewEnum.TEST_PLAN,
    icon: WorkOverviewIconEnum.TEST_PLAN,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: 'workbench.homePage.bugCount',
    value: WorkOverviewEnum.BUG_COUNT,
    icon: WorkOverviewIconEnum.BUG_COUNT,
    color: 'rgb(var(--danger-6))',
    count: 0,
  },
];

// 覆盖率
export const defaultCover = [
  {
    label: 'workbench.homePage.covered',
    value: '-',
    name: 'workbench.homePage.covered',
  },
  {
    label: 'workbench.homePage.notCover',
    value: '-',
    name: 'workbench.homePage.notCover',
  },
];
// 评审率
export const defaultReview = [
  {
    label: 'workbench.homePage.reviewed',
    value: '-',
    name: '',
  },
  {
    label: 'workbench.homePage.unReviewed',
    value: '-',
    name: '',
  },
];
// 通过率
export const defaultPass = [
  {
    label: 'workbench.homePage.havePassed',
    value: '-',
    name: '',
  },
  {
    label: 'workbench.homePage.notPass',
    value: '-',
    name: '',
  },
];
// 完成率
export const defaultComplete = [
  {
    label: 'common.completed',
    value: 10000,
    name: '',
  },
  {
    label: 'common.inProgress',
    value: 2000,
    name: '',
  },
  {
    label: 'workbench.homePage.unFinish',
    value: 2000,
    name: '',
  },
];
// 执行率
export const defaultExecution = [
  {
    label: 'common.unExecute',
    value: 10000,
    name: '',
  },
  {
    label: 'common.executed',
    value: 2000,
    name: '',
  },
];
// 遗留率
export const defaultLegacy = [
  {
    label: 'workbench.homePage.defectTotal',
    value: 10000,
    name: '',
  },
  {
    label: 'workbench.homePage.legacyDefectsNumber',
    value: 2000,
    name: '',
  },
];

export const defaultValueMap = (): Record<string, any> => {
  return {
    // 用例数量
    [WorkCardEnum.CASE_COUNT]: {
      review: {
        defaultList: cloneDeep(defaultCover),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.reviewRate',
      },
      pass: {
        defaultList: cloneDeep(defaultPass),
        color: ['#00C261', '#ED0303'],
        defaultName: 'workbench.homePage.passRate',
      },
    },
    // 关联用例数量
    [WorkCardEnum.ASSOCIATE_CASE_COUNT]: {
      cover: {
        defaultList: cloneDeep(defaultCover),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.coverRate',
      },
    },
    // 用例评审数
    [WorkCardEnum.REVIEW_CASE_COUNT]: {
      cover: {
        defaultList: cloneDeep(defaultCover),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.coverRate',
      },
    },
    // 测试计划数
    [WorkCardEnum.TEST_PLAN_COUNT]: {
      execute: {
        defaultList: cloneDeep(defaultExecution),
        color: [getVisualThemeColor('initItemStyleColor'), '#00C261'],
        defaultName: 'workbench.homePage.executeRate',
      },
      pass: {
        defaultList: cloneDeep(defaultPass),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.passRate',
      },
      complete: {
        defaultList: cloneDeep(defaultComplete),
        color: [getVisualThemeColor('initItemStyleColor'), '#3370FF', '#00C261', '#FF9964'],
        defaultName: 'workbench.homePage.completeRate',
      },
    },
    // 测试计划遗留缺陷
    [WorkCardEnum.PLAN_LEGACY_BUG]: {
      legacy: {
        defaultList: cloneDeep(defaultLegacy),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.legacyRate',
      },
    },
    // 缺陷数
    [WorkCardEnum.BUG_COUNT]: {
      legacy: {
        defaultList: cloneDeep(defaultLegacy),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.legacyRate',
      },
    },
    // 待我处理的缺陷
    [WorkCardEnum.HANDLE_BUG_BY_ME]: {
      legacy: {
        defaultList: cloneDeep(defaultLegacy),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.legacyRate',
      },
    },
    // 接口数量
    [WorkCardEnum.API_COUNT]: {
      cover: {
        defaultList: cloneDeep(defaultCover),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.coverRate',
      },
      complete: {
        defaultList: cloneDeep(defaultComplete),
        color: ['#00C261', '#3370FF', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.completeRate',
      },
    },
    // 我创建的缺陷
    [WorkCardEnum.CREATE_BUG_BY_ME]: {
      legacy: {
        defaultList: cloneDeep(defaultLegacy),
        color: ['#00C261', getVisualThemeColor('initItemStyleColor')],
        defaultName: 'workbench.homePage.legacyRate',
      },
    },
  };
};

// XX率饼图配置
export const commonRatePieOptions = {
  ...commonConfig,
  title: {
    show: true,
    text: '',
    left: 40,
    top: '26%',
    textStyle: {
      fontSize: 12,
      fontWeight: 'normal',
      color: '#959598',
    },
    triggerEvent: true, // 开启鼠标事件
    subtext: '0',
    subtextStyle: {
      fontSize: 12,
      color: getVisualThemeColor('subtextStyleColor'),
      fontWeight: 'bold',
      align: 'center',
      lineHeight: 3,
    },
    textAlign: 'center',
    tooltip: {
      ...toolTipConfig,
      position: 'right',
    },
  },
  tooltip: {
    ...toolTipConfig,
    position: 'right',
  },
  legend: {
    show: false,
  },
  series: {
    name: '',
    type: 'pie',
    color: [],
    radius: ['65%', '80%'],
    center: [44, '50%'],
    minAngle: 5,
    minShowLabelAngle: 10,
    avoidLabelOverlap: false,
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
    data: [],
  },
};

// 对应标识的入参跳转
export const NAV_NAVIGATION: Record<WorkNavValueEnum, any> = {
  [WorkNavValueEnum.CASE_REVIEWED]: { reviewStatus: [LastReviewResult.UN_PASS, LastReviewResult.PASS] }, // 有评审结果
  [WorkNavValueEnum.CASE_UN_REVIEWED]: {
    reviewStatus: [LastReviewResult.UN_REVIEWED, LastReviewResult.UNDER_REVIEWED, LastReviewResult.RE_REVIEWED], // 没有评审结果
  },
  [WorkNavValueEnum.CASE_REVIEWED_PASS]: { reviewStatus: [LastReviewResult.PASS] }, // 评审通过
  [WorkNavValueEnum.CASE_REVIEWED_UN_PASS]: {
    reviewStatus: [
      LastReviewResult.UN_REVIEWED,
      LastReviewResult.UNDER_REVIEWED,
      LastReviewResult.RE_REVIEWED,
      LastReviewResult.UN_PASS,
    ], // 评审不通过
  },
  [WorkNavValueEnum.CASE_ASSOCIATED]: {
    associateCase: [true], // 已关联用例
  },
  [WorkNavValueEnum.CASE_NOT_ASSOCIATED]: {
    associateCase: [false], // 没有关联用例
  },
  [WorkNavValueEnum.API_COUNT_DONE]: {
    status: [RequestDefinitionStatus.DONE], // 接口数-已完成
  },
  [WorkNavValueEnum.API_COUNT_PROCESSING]: {
    status: [RequestDefinitionStatus.PROCESSING], // 接口数-进行中
  },
  [WorkNavValueEnum.API_COUNT_DEBUGGING]: {
    status: [RequestDefinitionStatus.DEBUGGING], // 接口数-联调中
  },
  [WorkNavValueEnum.API_COUNT_DEPRECATED]: {
    status: [RequestDefinitionStatus.DEPRECATED], // 接口数-已废弃
  },
  [WorkNavValueEnum.API_COUNT_EXECUTE_FAKE_ERROR]: {
    lastReportStatus: [ExecuteResultEnum.FAKE_ERROR], // 接口用例-执行结果-误报
  },
  [WorkNavValueEnum.API_COUNT_EXECUTE_SUCCESS]: {
    lastReportStatus: [ExecuteResultEnum.SUCCESS], // 接口用例-执行结果-已通过
  },
  [WorkNavValueEnum.API_COUNT_EXECUTE_ERROR]: {
    lastReportStatus: [ExecuteResultEnum.ERROR], // 接口用例-执行结果-未通过
  },
  [WorkNavValueEnum.API_COUNT_EXECUTED_RESULT]: {
    lastReportStatus: [ExecuteResultEnum.SUCCESS, ExecuteResultEnum.ERROR, ExecuteResultEnum.FAKE_ERROR], // 接口用例-有执行结果
  },
  [WorkNavValueEnum.API_COUNT_EXECUTED_NOT_RESULT]: {
    lastReportStatus: [''], // 接口用例-无执行结果
  },
  [WorkNavValueEnum.SCENARIO_COUNT_EXECUTE_FAKE_ERROR]: {
    lastReportStatus: [ExecuteResultEnum.FAKE_ERROR], // 场景用例-执行结果-误报
  },
  [WorkNavValueEnum.SCENARIO_COUNT_EXECUTE_SUCCESS]: {
    lastReportStatus: [ExecuteResultEnum.SUCCESS], // 场景用例-执行结果-已通过
  },
  [WorkNavValueEnum.SCENARIO_COUNT_EXECUTE_ERROR]: {
    lastReportStatus: [ExecuteResultEnum.ERROR], // 场景用例-执行结果-未通过
  },
  [WorkNavValueEnum.SCENARIO_COUNT_EXECUTED_RESULT]: {
    lastReportStatus: [ExecuteResultEnum.SUCCESS, ExecuteResultEnum.ERROR, ExecuteResultEnum.FAKE_ERROR], // 场景用例-有执行结果
  },
  /**
   * 接口
   */
  [WorkNavValueEnum.API_COUNT_COVER]: {
    coverFrom: ['apiDefinition'], // 接口覆盖
  },
  [WorkNavValueEnum.API_COUNT_UN_COVER]: {
    unCoverFrom: ['apiDefinition'], // 接口未覆盖
  },
  /**
   * 接口用例
   */
  [WorkNavValueEnum.API_CASE_COUNT_COVER]: {
    coverFrom: ['apiCase'], // 接口用例覆盖
  },
  [WorkNavValueEnum.API_CASE_COUNT_UN_COVER]: {
    unCoverFrom: ['apiCase'], // 接口用例未覆盖
  },
  /**
   * 场景用例
   */
  [WorkNavValueEnum.SCENARIO_COVER]: {
    coverFrom: ['apiScenario'], // 接口场景覆盖
  },
  [WorkNavValueEnum.SCENARIO_UN_COVER]: {
    unCoverFrom: ['apiScenario'], // 接口场景未覆盖
  },

  [WorkNavValueEnum.SCENARIO_COUNT_EXECUTED_NOT_RESULT]: { lastReportStatus: [''] }, // 场景用例-无执行结果
  /**
   * 测试计划数量
   */
  [WorkNavValueEnum.TEST_PLAN_COMPLETED]: { status: [TestPlanStatusEnum.COMPLETED] }, // 测试计划-已完成
  [WorkNavValueEnum.TEST_PLAN_UNDERWAY]: { status: [TestPlanStatusEnum.UNDERWAY] }, // 测试计划-进行中
  [WorkNavValueEnum.TEST_PLAN_PREPARED]: { status: [TestPlanStatusEnum.PREPARED] }, // 测试计划-未开始
  [WorkNavValueEnum.TEST_PLAN_ARCHIVED]: { status: [TestPlanStatusEnum.ARCHIVED] }, // 测试计划-已归档
  [WorkNavValueEnum.TEST_PLAN_PASSED]: {
    passed: ['PASSED'], // 测试计划-已通过
  },
  [WorkNavValueEnum.TEST_PLAN_NOT_PASS]: {
    passed: ['NOT_PASSED'], // 测试计划-未通过
  },
  [WorkNavValueEnum.TEST_PLAN_PASSED_ARCHIVED]: {
    archivedPassed: ['PASSED'], // 测试计划-已归档-已通过
  },
  [WorkNavValueEnum.TEST_PLAN_NOT_PASS_ARCHIVED]: {
    archivedPassed: ['NOT_PASSED'], // 测试计划-已归档-未通过
  },
  /**
   * 测试计划遗留缺陷
   */
  [WorkNavValueEnum.TEST_PLAN_LEGACY]: {
    relatedToPlan: true,
    unresolved: true,
  },
  [WorkNavValueEnum.TEST_PLAN_BUG]: {
    relatedToPlan: true,
    unresolved: false,
  },
  [WorkNavValueEnum.BUG_COUNT]: {
    boardCount: true,
  },
  [WorkNavValueEnum.BUG_COUNT_LEGACY]: {
    unresolved: true,
  },
  [WorkNavValueEnum.BUG_COUNT_BY_ME]: {
    createByMe: true,
    unresolved: false,
  },
  [WorkNavValueEnum.BUG_COUNT_BY_ME_LEGACY]: {
    createByMe: true,
    unresolved: true,
  },
  [WorkNavValueEnum.BUG_HANDLE_BY_ME]: {
    assignedToMe: true,
    unresolved: false,
  },
  [WorkNavValueEnum.BUG_HANDLE_BY_ME_LEGACY]: {
    assignedToMe: true,
    unresolved: true,
  },
};

export default {};

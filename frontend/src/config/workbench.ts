import { cloneDeep } from 'lodash-es';

import { commonConfig, toolTipConfig } from '@/config/testPlan';

import type { ModuleCardItem } from '@/models/workbench/homePage';
import { WorkCardEnum, WorkOverviewEnum, WorkOverviewIconEnum } from '@/enums/workbenchEnum';

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

export const defaultValueMap: Record<string, any> = {
  // 用例数量
  [WorkCardEnum.CASE_COUNT]: {
    review: {
      defaultList: cloneDeep(defaultCover),
      color: ['#00C261', '#D4D4D8'],
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
      color: ['#00C261', '#D4D4D8'],
      defaultName: 'workbench.homePage.coverRate',
    },
  },
  // 用例评审数
  [WorkCardEnum.REVIEW_CASE_COUNT]: {
    cover: {
      defaultList: cloneDeep(defaultCover),
      color: ['#00C261', '#D4D4D8'],
      defaultName: 'workbench.homePage.coverRate',
    },
  },
  // 测试计划数
  [WorkCardEnum.TEST_PLAN_COUNT]: {
    execute: {
      defaultList: cloneDeep(defaultExecution),
      color: ['#D4D4D8', '#00C261'],
      defaultName: 'workbench.homePage.executeRate',
    },
    pass: {
      defaultList: cloneDeep(defaultPass),
      color: ['#D4D4D8', '#00C261'],
      defaultName: 'workbench.homePage.passRate',
    },
    complete: {
      defaultList: cloneDeep(defaultComplete),
      color: ['#00C261', '#3370FF', '#D4D4D8'],
      defaultName: 'workbench.homePage.completeRate',
    },
  },
  // 测试计划遗留缺陷
  [WorkCardEnum.PLAN_LEGACY_BUG]: {
    legacy: {
      defaultList: cloneDeep(defaultLegacy),
      color: ['#D4D4D8', '#00C261'],
      defaultName: 'workbench.homePage.legacyRate',
    },
  },
  // 缺陷数
  [WorkCardEnum.BUG_COUNT]: {
    legacy: {
      defaultList: cloneDeep(defaultLegacy),
      color: ['#D4D4D8', '#00C261'],
      defaultName: 'workbench.homePage.legacyRate',
    },
  },
  // 待我处理的缺陷
  [WorkCardEnum.HANDLE_BUG_BY_ME]: {
    legacy: {
      defaultList: cloneDeep(defaultLegacy),
      color: ['#D4D4D8', '#00C261'],
      defaultName: 'workbench.homePage.legacyRate',
    },
  },
  // 接口数量
  [WorkCardEnum.API_COUNT]: {
    cover: {
      defaultList: cloneDeep(defaultCover),
      color: ['#00C261', '#D4D4D8'],
      defaultName: 'workbench.homePage.coverRate',
    },
    complete: {
      defaultList: cloneDeep(defaultComplete),
      color: ['#00C261', '#3370FF', '#D4D4D8'],
      defaultName: 'workbench.homePage.completeRate',
    },
  },
  // 我创建的缺陷
  [WorkCardEnum.CREATE_BUG_BY_ME]: {
    legacy: {
      defaultList: cloneDeep(defaultLegacy),
      color: ['#D4D4D8', '#00C261'],
      defaultName: 'workbench.homePage.legacyRate',
    },
  },
};

// XX率饼图配置
export const commonRatePieOptions = {
  ...commonConfig,
  title: {
    show: true,
    text: '',
    left: 26,
    top: '20%',
    textStyle: {
      fontSize: 12,
      fontWeight: 'normal',
      color: '#959598',
    },
    triggerEvent: true, // 开启鼠标事件
    subtext: '0',
    subtextStyle: {
      fontSize: 12,
      color: '#323233',
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
    padAngle: 2,
    radius: ['85%', '100%'],
    center: [30, '50%'],
    avoidLabelOverlap: false,
    label: {
      show: false,
      position: 'center',
    },
    emphasis: {
      scale: false, // 禁用放大效果
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

export default {};

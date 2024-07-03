import { TestPlanRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const TestPlan: AppRouteRecordRaw = {
  path: '/test-plan',
  name: TestPlanRouteEnum.TEST_PLAN,
  redirect: '/test-plan/testPlanIndex',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.testPlan',
    collapsedLocale: 'menu.testPlanShort',
    icon: 'icon-a-icon_test-tracking_filled1',
    order: 2,
    hideChildrenInMenu: true,
    roles: ['PROJECT_TEST_PLAN:READ', 'PROJECT_TEST_PLAN_REPORT:READ'],
  },
  children: [
    // 测试计划
    {
      path: 'testPlanIndex',
      name: TestPlanRouteEnum.TEST_PLAN_INDEX,
      component: () => import('@/views/test-plan/testPlan/index.vue'),
      meta: {
        locale: 'menu.testPlanShort',
        roles: ['PROJECT_TEST_PLAN:READ'],
        isTopMenu: true,
      },
    },
    {
      path: 'testPlanReport',
      name: TestPlanRouteEnum.TEST_PLAN_REPORT,
      component: () => import('@/views/test-plan/report/index.vue'),
      meta: {
        locale: 'menu.apiTest.report',
        roles: ['PROJECT_TEST_PLAN_REPORT:READ'],
        isTopMenu: true,
      },
    },
    {
      path: 'testPlanReportDetail',
      name: TestPlanRouteEnum.TEST_PLAN_REPORT_DETAIL,
      component: () => import('@/views/test-plan/report/detail/detail.vue'),
      meta: {
        locale: 'menu.apiTest.reportDetail',
        roles: ['PROJECT_TEST_PLAN_REPORT:READ'],
        breadcrumbs: [
          {
            name: TestPlanRouteEnum.TEST_PLAN_REPORT,
            locale: 'menu.apiTest.report',
          },
          {
            name: TestPlanRouteEnum.TEST_PLAN_REPORT_DETAIL,
            locale: 'menu.apiTest.reportDetail',
          },
        ],
      },
    },
    // 测试计划详情
    {
      path: 'testPlanIndexDetail',
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
      component: () => import('@/views/test-plan/testPlan/detail/index.vue'),
      meta: {
        locale: 'menu.testPlan.testPlanDetail',
        roles: ['PROJECT_TEST_PLAN:READ'],
        breadcrumbs: [
          {
            name: TestPlanRouteEnum.TEST_PLAN_INDEX,
            locale: 'menu.testPlan',
          },
          {
            name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
            locale: 'menu.testPlan.testPlanDetail',
          },
        ],
      },
    },
    // 自定义配置报告
    {
      path: 'testPlanIndexConfig',
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_CONFIG,
      component: () => import('@/views/test-plan/report/detail/configReport.vue'),
      meta: {
        locale: 'testPlan.planConfigReport',
        roles: ['PROJECT_TEST_PLAN_REPORT:READ'],
        isTopMenu: false,
      },
    },
    // 测试计划-测试计划详情-功能用例详情
    {
      path: 'testPlanIndexDetailFeatureCaseDetail',
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL,
      component: () => import('@/views/test-plan/testPlan/detail/featureCase/detail/index.vue'),
      meta: {
        locale: 'menu.testPlan.testPlanDetail',
        roles: ['PROJECT_TEST_PLAN:READ'],
        breadcrumbs: [
          {
            name: TestPlanRouteEnum.TEST_PLAN_INDEX,
            locale: 'menu.testPlan',
          },
          {
            name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
            locale: 'menu.testPlan.testPlanDetail',
            isBack: true,
            query: ['id'],
          },
          {
            name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL,
            locale: 'menu.caseManagement.caseManagementCaseDetail',
          },
        ],
      },
    },
  ],
};

export default TestPlan;

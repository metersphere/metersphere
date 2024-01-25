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
    icon: 'icon-icon_test-tracking_filled',
    order: 1,
    hideChildrenInMenu: true,
  },
  children: [
    // 测试计划
    {
      path: 'testPlanIndex',
      name: TestPlanRouteEnum.TEST_PLAN_INDEX,
      component: () => import('@/views/test-plan/testPlan/index.vue'),
      meta: {
        locale: 'menu.testPlan',
        roles: ['PROJECT_TEST_PLAN:READ'],
        isTopMenu: true,
      },
    },
  ],
};

export default TestPlan;

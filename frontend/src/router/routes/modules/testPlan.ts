import { DEFAULT_LAYOUT } from '../base';
import { TestPlanRouteEnum } from '@/enums/routeEnum';

import type { AppRouteRecordRaw } from '../types';

const TestPlan: AppRouteRecordRaw = {
  path: '/test-plan',
  name: TestPlanRouteEnum.TEST_PLAN,
  redirect: '/test-plan/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.testPlan',
    icon: 'icon-icon_test-tracking_filled',
    order: 1,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'testPlanIndex',
      component: () => import('@/views/test-plan/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default TestPlan;

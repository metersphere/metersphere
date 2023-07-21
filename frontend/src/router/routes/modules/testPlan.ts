import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const TestPlan: AppRouteRecordRaw = {
  path: '/test-plan',
  name: menuEnum.TESTPLAN,
  redirect: '/test-plan/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.testPlan',
    icon: 'icon_test-tracking_filled',
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

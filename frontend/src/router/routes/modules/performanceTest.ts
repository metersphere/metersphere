import { PerformanceTestRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const PerformanceTest: AppRouteRecordRaw = {
  path: '/performance-test',
  name: PerformanceTestRouteEnum.PERFORMANCE_TEST,
  redirect: '/performance-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.performanceTest',
    icon: 'icon-icon_performance-test-filled',
    order: 6,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'performanceTestIndex',
      component: () => import('@/views/performance-test/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default PerformanceTest;

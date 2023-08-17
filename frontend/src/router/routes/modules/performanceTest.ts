import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const PerformanceTest: AppRouteRecordRaw = {
  path: '/performance-test',
  name: menuEnum.PERFORMANCE_TEST,
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

import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const PerformanceTest: AppRouteRecordRaw = {
  path: '/performtest-test',
  name: menuEnum.PERFORMANCETEST,
  redirect: '/performtest-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.performanceTest',
    icon: 'icon_performance-test-filled',
    order: 6,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'apiTestList',
      component: () => import('@/views/performance-test/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default PerformanceTest;

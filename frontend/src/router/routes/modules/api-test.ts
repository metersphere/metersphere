import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const ApiTest: AppRouteRecordRaw = {
  path: '/api-test',
  name: 'apiTest',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.apiTest',
    icon: 'icon-dashboard',
    order: 0,
  },
  children: [
    {
      path: 'list',
      name: 'apiTest',
      component: () => import('@/views/api-test/index.vue'),
      meta: {
        locale: 'menu.apiTest',
        roles: ['*'],
        icon: 'icon-computer',
      },
    },
  ],
};

export default ApiTest;

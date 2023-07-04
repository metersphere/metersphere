import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const ApiTest: AppRouteRecordRaw = {
  path: '/api-test',
  name: 'apiTest',
  redirect: '/api-test/list',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.apiTest',
    icon: 'icon-dashboard',
    order: 0,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'list',
      name: 'apiTestList',
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

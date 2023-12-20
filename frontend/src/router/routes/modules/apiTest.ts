import { ApiTestRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const ApiTest: AppRouteRecordRaw = {
  path: '/api-test',
  name: ApiTestRouteEnum.API_TEST,
  redirect: '/api-test/debug',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.apiTest',
    icon: 'icon-icon_api-test-filled',
    order: 4,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'debug',
      name: ApiTestRouteEnum.API_TEST_DEBUG,
      component: () => import('@/views/api-test/debug/index.vue'),
      meta: {
        locale: 'menu.apiTest.debug',
        roles: ['*'],
        isTopMenu: true,
      },
    },
  ],
};

export default ApiTest;

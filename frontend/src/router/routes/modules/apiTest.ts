import { DEFAULT_LAYOUT } from '../base';
import { ApiTestRouteEnum } from '@/enums/routeEnum';

import type { AppRouteRecordRaw } from '../types';

const ApiTest: AppRouteRecordRaw = {
  path: '/api-test',
  name: ApiTestRouteEnum.API_TEST,
  redirect: '/api-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.apiTest',
    icon: 'icon-icon_api-test-filled',
    order: 4,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'apiTestIndex',
      component: () => import('@/views/api-test/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default ApiTest;

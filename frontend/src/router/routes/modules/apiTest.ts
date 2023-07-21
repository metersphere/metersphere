import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const ApiTest: AppRouteRecordRaw = {
  path: '/api-test',
  name: menuEnum.APITEST,
  redirect: '/api-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.apiTest',
    icon: 'icon_api-test-filled',
    order: 4,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'ApiTestIndex',
      component: () => import('@/views/api-test/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default ApiTest;

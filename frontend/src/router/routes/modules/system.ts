import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const ApiTest: AppRouteRecordRaw = {
  path: '/system',
  name: 'system',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.settings',
    icon: 'icon-dashboard',
    order: 0,
  },
  children: [
    {
      path: 'user',
      name: 'user',
      component: () => import('@/views/system/user/index.vue'),
      meta: {
        locale: 'menu.settings.user',
        roles: ['*'],
      },
    },
    {
      path: 'usergroup',
      name: 'usergroup',
      component: () => import('@/views/system/usergroup/index.vue'),
      meta: {
        locale: 'menu.settings.usergroup',
        roles: ['*'],
      },
    },
  ],
};

export default ApiTest;

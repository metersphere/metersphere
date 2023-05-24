import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const MINDER: AppRouteRecordRaw = {
  path: '/minder',
  name: 'minder',
  component: DEFAULT_LAYOUT,
  redirect: '/minder/index',
  meta: {
    locale: 'menu.minder',
    icon: 'icon-dashboard',
    order: 0,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'minder',
      component: () => import('@/views/minder/index.vue'),
      meta: {
        locale: 'menu.minder',
        roles: ['*'],
      },
    },
  ],
};

export default MINDER;

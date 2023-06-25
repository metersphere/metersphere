import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const System: AppRouteRecordRaw = {
  path: '/setting',
  name: 'setting',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.settings',
    icon: 'icon-dashboard',
    order: 0,
  },
  children: [
    {
      path: 'system',
      name: 'settingSystem',
      redirect: '/setting/system/user',
      component: null,
      meta: {
        locale: 'menu.settings.system',
        roles: ['*'],
        hideChildrenInMenu: true,
      },
      children: [
        {
          path: 'user',
          name: 'settingSystemUser',
          component: () => import('@/views/system/user/index.vue'),
          meta: {
            locale: 'menu.settings.user',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'usergroup',
          name: 'settingSystemUsergroup',
          component: () => import('@/views/system/usergroup/index.vue'),
          meta: {
            locale: 'menu.settings.usergroup',
            roles: ['*'],
            isTopMenu: true,
          },
        },
      ],
    },
  ],
};

export default System;

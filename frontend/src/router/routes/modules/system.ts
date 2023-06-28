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
            locale: 'menu.settings.system.user',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'usergroup',
          name: 'settingSystemUsergroup',
          component: () => import('@/views/system/usergroup/index.vue'),
          meta: {
            locale: 'menu.settings.system.usergroup',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'resourcePool',
          name: 'settingSystemResourcePool',
          component: () => import('@/views/system/resourcePool/index.vue'),
          meta: {
            locale: 'menu.settings.system.resourcePool',
            roles: ['*'],
            isTopMenu: true,
            breadcrumbs: [
              {
                name: 'settingSystemResourcePool',
                locale: 'menu.settings.system.resourcePool',
              },
            ],
          },
        },
        {
          path: 'resourcePoolDetail',
          name: 'settingSystemResourcePoolDetail',
          component: () => import('@/views/system/resourcePool/detail.vue'),
          meta: {
            locale: 'menu.settings.system.resourcePoolDetail',
            roles: ['*'],
            breadcrumbs: [
              {
                name: 'settingSystemResourcePool',
                locale: 'menu.settings.system.resourcePool',
              },
              {
                name: 'settingSystemResourcePoolDetail',
                locale: 'menu.settings.system.resourcePoolDetail',
                editTag: 'id',
                editLocale: 'menu.settings.system.resourcePoolEdit',
              },
            ],
          },
        },
      ],
    },
  ],
};

export default System;

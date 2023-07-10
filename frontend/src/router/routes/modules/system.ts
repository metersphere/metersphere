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
            locale: 'menu.settings.system.pluginmanger',
            roles: ['*'],
            isTopMenu: true,
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
        {
          path: 'pluginmanger',
          name: 'settingSystemPluginManger',
          component: () => import('@/views/system/pluginManager/index.vue'),
          meta: {
            locale: 'menu.settings.system.pluginmanger',
            roles: ['*'],
            isTopMenu: true,
            breadcrumbs: [
              {
                name: 'settingSystemResourcePool',
                locale: 'menu.settings.system.pluginmanger',
              },
            ],
          },
        },
      ],
    },
    {
      path: 'organization',
      name: 'settingOrganization',
      redirect: '/setting/organization/member',
      component: null,
      meta: {
        locale: 'menu.settings.organization',
        roles: ['*'],
        hideChildrenInMenu: true,
      },
      children: [
        {
          path: 'member',
          name: 'settingOrganizationMember',
          component: () => import('@/views/organization/member/index.vue'),
          meta: {
            locale: 'menu.settings.organization.member',
            roles: ['*'],
            isTopMenu: true,
          },
        },
      ],
    },
  ],
};

export default System;

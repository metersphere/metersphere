import { DEFAULT_LAYOUT } from '../base';
import { SettingRouteEnum } from '@/enums/routeEnum';

import type { AppRouteRecordRaw } from '../types';

const Setting: AppRouteRecordRaw = {
  path: '/setting',
  name: SettingRouteEnum.SETTING,
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.settings',
    icon: 'icon-a-icon_system_settings',
    order: 8,
  },
  children: [
    {
      path: 'system',
      name: SettingRouteEnum.SETTING_SYSTEM,
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
          name: SettingRouteEnum.SETTING_SYSTEM_USER_SINGLE,
          component: () => import('@/views/setting/system/user/index.vue'),
          meta: {
            locale: 'menu.settings.system.user',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'usergroup',
          name: SettingRouteEnum.SETTING_SYSTEM_USER_GROUP,
          component: () => import('@/views/setting/system/usergroup/index.vue'),
          meta: {
            locale: 'menu.settings.system.usergroup',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'organization-and-project',
          name: SettingRouteEnum.SETTING_SYSTEM_ORGANIZATION,
          component: () => import('@/views/setting/system/organizationAndProject/index.vue'),
          meta: {
            locale: 'menu.settings.system.organizationAndProject',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'parameter',
          name: SettingRouteEnum.SETTING_SYSTEM_PARAMETER,
          component: () => import('@/views/setting/system/config/index.vue'),
          meta: {
            locale: 'menu.settings.system.parameter',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'resourcePool',
          name: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL,
          component: () => import('@/views/setting/system/resourcePool/index.vue'),
          meta: {
            locale: 'menu.settings.system.resourcePool',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'resourcePoolDetail',
          name: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL_DETAIL,
          component: () => import('@/views/setting/system/resourcePool/detail.vue'),
          meta: {
            locale: 'menu.settings.system.resourcePoolDetail',
            roles: ['*'],
            breadcrumbs: [
              {
                name: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL,
                locale: 'menu.settings.system.resourcePool',
              },
              {
                name: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL_DETAIL,
                locale: 'menu.settings.system.resourcePoolDetail',
                editTag: 'id',
                editLocale: 'menu.settings.system.resourcePoolEdit',
              },
            ],
          },
        },
        {
          path: 'authorizedmanagement',
          name: SettingRouteEnum.SETTING_SYSTEM_AUTHORIZED_MANAGEMENT,
          component: () => import('@/views/setting/system/authorizedManagement/index.vue'),
          meta: {
            locale: 'menu.settings.system.authorizedManagement',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'log',
          name: SettingRouteEnum.SETTING_SYSTEM_LOG,
          component: () => import('@/views/setting/system/log/index.vue'),
          meta: {
            locale: 'menu.settings.system.log',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'pluginManager',
          name: SettingRouteEnum.SETTING_SYSTEM_PLUGIN_MANAGEMENT,
          component: () => import('@/views/setting/system/pluginManager/index.vue'),
          meta: {
            locale: 'menu.settings.system.pluginManager',
            roles: ['*'],
            isTopMenu: true,
          },
        },
      ],
    },
    {
      path: 'organization',
      name: SettingRouteEnum.SETTING_ORGANIZATION,
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
          name: SettingRouteEnum.SETTING_ORGANIZATION_MEMBER,
          component: () => import('@/views/setting/organization/member/index.vue'),
          meta: {
            locale: 'menu.settings.organization.member',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'serviceIntegration',
          name: SettingRouteEnum.SETTING_ORGANIZATION_SERVICE,
          component: () => import('@/views/setting/organization/serviceIntegration/index.vue'),
          meta: {
            locale: 'menu.settings.organization.serviceIntegration',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'log',
          name: SettingRouteEnum.SETTING_ORGANIZATION_LOG,
          component: () => import('@/views/setting/organization/log/index.vue'),
          meta: {
            locale: 'menu.settings.organization.log',
            roles: ['*'],
            isTopMenu: true,
          },
        },
      ],
    },
  ],
};

export default Setting;

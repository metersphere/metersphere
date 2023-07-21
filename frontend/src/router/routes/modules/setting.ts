import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const Setting: AppRouteRecordRaw = {
  path: '/setting',
  name: 'setting',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.settings',
    icon: 'icon-a-icon_system_settings',
    order: 8,
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
          component: () => import('@/views/setting/system/user/index.vue'),
          meta: {
            locale: 'menu.settings.system.user',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'usergroup',
          name: 'settingSystemUsergroup',
          component: () => import('@/views/setting/system/usergroup/index.vue'),
          meta: {
            locale: 'menu.settings.system.usergroup',
            roles: ['*'],
            isTopMenu: true,
          },
        },
        {
          path: 'resourcePool',
          name: 'settingSystemResourcePool',
          component: () => import('@/views/setting/system/resourcePool/index.vue'),
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
          component: () => import('@/views/setting/system/resourcePool/detail.vue'),
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
          component: () => import('@/views/setting/system/pluginManager/index.vue'),
          meta: {
            locale: 'menu.settings.system.pluginmanger',
            roles: ['*'],
            isTopMenu: true,
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
          component: () => import('@/views/setting/organization/member/index.vue'),
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

export default Setting;

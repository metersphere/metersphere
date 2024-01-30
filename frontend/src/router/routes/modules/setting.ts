// import useLicenseStore from '@/store/modules/setting/license';

import { SettingRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

// const licenseStore = useLicenseStore();

const Setting: AppRouteRecordRaw = {
  path: '/setting',
  name: SettingRouteEnum.SETTING,
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.settings',
    icon: 'icon-a-icon_system_settings',
    order: 8,
    roles: [
      'SYSTEM_USER:READ',
      'SYSTEM_USER_ROLE:READ',
      'SYSTEM_ORGANIZATION_PROJECT:READ',
      'SYSTEM_PARAMETER_SETTING_BASE:READ',
      'SYSTEM_TEST_RESOURCE_POOL:READ',
      'SYSTEM_AUTH:READ',
      'SYSTEM_PLUGIN:READ',
      'ORGANIZATION_MEMBER:READ',
      'ORGANIZATION_USER_ROLE:READ',
      'ORGANIZATION_PROJECT:READ',
      'SYSTEM_SERVICE_INTEGRATION:READ',
      'ORGANIZATION_TEMPLATE:READ',
    ],
  },
  children: [
    {
      path: 'system',
      name: SettingRouteEnum.SETTING_SYSTEM,
      redirect: '/setting/system/user',
      component: null,
      meta: {
        locale: 'menu.settings.system',
        roles: [
          'SYSTEM_USER:READ',
          'SYSTEM_USER_ROLE:READ',
          'SYSTEM_ORGANIZATION_PROJECT:READ',
          'SYSTEM_PARAMETER_SETTING_BASE:READ',
          'SYSTEM_TEST_RESOURCE_POOL:READ',
          'SYSTEM_AUTH:READ',
          'SYSTEM_PLUGIN:READ',
        ],
        hideChildrenInMenu: true,
      },
      children: [
        {
          path: 'user',
          name: SettingRouteEnum.SETTING_SYSTEM_USER_SINGLE,
          component: () => import('@/views/setting/system/user/index.vue'),
          meta: {
            locale: 'menu.settings.system.user',
            roles: ['SYSTEM_USER:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'usergroup',
          name: SettingRouteEnum.SETTING_SYSTEM_USER_GROUP,
          component: () => import('@/views/setting/system/usergroup/systemUserGroup.vue'),
          meta: {
            locale: 'menu.settings.system.usergroup',
            roles: ['SYSTEM_USER_ROLE:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'organization-and-project',
          name: SettingRouteEnum.SETTING_SYSTEM_ORGANIZATION,
          component: () => import('@/views/setting/system/organizationAndProject/index.vue'),
          meta: {
            locale: 'menu.settings.system.organizationAndProject',
            roles: ['SYSTEM_ORGANIZATION_PROJECT:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'parameter',
          name: SettingRouteEnum.SETTING_SYSTEM_PARAMETER,
          component: () => import('@/views/setting/system/config/index.vue'),
          meta: {
            locale: 'menu.settings.system.parameter',
            roles: ['SYSTEM_PARAMETER_SETTING_BASE:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'resourcePool',
          name: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL,
          component: () => import('@/views/setting/system/resourcePool/index.vue'),
          meta: {
            locale: 'menu.settings.system.resourcePool',
            roles: ['SYSTEM_TEST_RESOURCE_POOL:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'resourcePoolDetail',
          name: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL_DETAIL,
          component: () => import('@/views/setting/system/resourcePool/detail.vue'),
          meta: {
            locale: 'menu.settings.system.resourcePoolDetail',
            roles: ['SYSTEM_TEST_RESOURCE_POOL:READ'],
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
            roles: ['SYSTEM_AUTH:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'log',
          name: SettingRouteEnum.SETTING_SYSTEM_LOG,
          component: () => import('@/views/setting/system/log/index.vue'),
          meta: {
            locale: 'menu.settings.system.log',
            roles: ['SYSTEM_LOG:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'pluginManager',
          name: SettingRouteEnum.SETTING_SYSTEM_PLUGIN_MANAGEMENT,
          component: () => import('@/views/setting/system/pluginManager/index.vue'),
          meta: {
            locale: 'menu.settings.system.pluginManager',
            roles: ['SYSTEM_PLUGIN:READ'],
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
        roles: [
          'ORGANIZATION_MEMBER:READ',
          'ORGANIZATION_USER_ROLE:READ',
          'ORGANIZATION_PROJECT:READ',
          'SYSTEM_SERVICE_INTEGRATION:READ',
          'ORGANIZATION_TEMPLATE:READ',
        ],
        hideChildrenInMenu: true,
      },
      children: [
        {
          path: 'member',
          name: SettingRouteEnum.SETTING_ORGANIZATION_MEMBER,
          component: () => import('@/views/setting/organization/member/index.vue'),
          meta: {
            locale: 'menu.settings.organization.member',
            roles: ['ORGANIZATION_MEMBER:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'usergroup',
          name: SettingRouteEnum.SETTING_ORGANIZATION_USER_GROUP,
          component: () => import('@/views/setting/organization/usergroup/orgUserGroup.vue'),
          meta: {
            locale: 'menu.settings.organization.userGroup',
            roles: ['ORGANIZATION_USER_ROLE:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'project',
          name: SettingRouteEnum.SETTING_ORGANIZATION_PROJECT,
          component: () => import('@/views/setting/organization/project/orgProject.vue'),
          meta: {
            locale: 'menu.settings.organization.project',
            roles: ['ORGANIZATION_PROJECT:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'serviceIntegration',
          name: SettingRouteEnum.SETTING_ORGANIZATION_SERVICE,
          component: () => import('@/views/setting/organization/serviceIntegration/index.vue'),
          meta: {
            locale: 'menu.settings.organization.serviceIntegration',
            roles: ['SYSTEM_SERVICE_INTEGRATION:READ'],
            isTopMenu: true,
          },
        },
        {
          path: 'template',
          name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
          component: () => import('@/views/setting/organization/template/index.vue'),
          meta: {
            locale: 'menu.settings.organization.template',
            roles: ['ORGANIZATION_TEMPLATE:READ'],
            isTopMenu: true,
          },
        },
        // 模板列表-模版字段设置
        {
          path: 'templateFiledSetting',
          name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
          component: () => import('@/views/setting/organization/template/components/ordFieldSetting.vue'),
          meta: {
            locale: 'menu.settings.organization.templateFieldSetting',
            roles: ['ORGANIZATION_TEMPLATE:READ'],
            breadcrumbs: [
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
                locale: 'menu.settings.organization.template',
              },
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
                locale: 'menu.settings.organization.templateFieldSetting',
                editLocale: 'menu.settings.organization.templateFieldSetting',
              },
            ],
          },
        },
        // 模版管理-模版列表
        {
          path: 'templateManagement',
          name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
          component: () => import('@/views/setting/organization/template/components/templateManagement.vue'),
          meta: {
            locale: 'menu.settings.organization.templateManagementList',
            roles: ['ORGANIZATION_TEMPLATE:READ'],
            breadcrumbs: [
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
                locale: 'menu.settings.organization.template',
              },
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                locale: 'menu.settings.organization.templateManagementList',
                editLocale: 'menu.settings.organization.templateManagementList',
              },
            ],
          },
        },
        // 模板列表-模板管理-创建&编辑模版
        {
          path: 'templateManagementDetail/:mode?',
          name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_DETAIL,
          component: () => import('@/views/setting/organization/template/components/templateDetail.vue'),
          meta: {
            locale: 'menu.settings.organization.templateManagementDetail',
            roles: ['*'],
            breadcrumbs: [
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
                locale: 'menu.settings.organization.template',
              },
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                locale: 'menu.settings.organization.templateManagementList',
              },
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                locale: 'menu.settings.organization.templateManagementDetail',
                editTag: 'id',
                editLocale: 'menu.settings.organization.templateManagementEdit',
              },
            ],
          },
        },
        // 模板列表-模板管理-工作流
        {
          path: 'templateWorkFlow',
          name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW,
          component: () => import('@/views/setting/organization/template/components/workFlowTableIndex.vue'),
          meta: {
            locale: 'menu.settings.organization.templateManagementWorkFlow',
            roles: ['*'],
            breadcrumbs: [
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
                locale: 'menu.settings.organization.bugTemplate',
              },
              {
                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW,
                locale: 'menu.settings.organization.templateManagementWorkFlow',
              },
            ],
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

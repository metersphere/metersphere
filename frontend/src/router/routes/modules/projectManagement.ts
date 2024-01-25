import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const ProjectManagement: AppRouteRecordRaw = {
  path: '/project-management',
  name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT,
  redirect: '/project-management/permission',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.projectManagement',
    icon: 'icon-icon_project-settings-filled',
    order: 7,
    hideChildrenInMenu: true,
  },
  children: [
    // 项目与权限
    {
      path: 'permission',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION,
      component: () => import('@/views/project-management/projectAndPermission/index.vue'),
      redirect: '/project-management/permission/basicInfo',
      meta: {
        locale: 'menu.projectManagement.projectPermission',
        roles: ['PROJECT_USER:READ'],
        isTopMenu: true,
      },
      children: [
        // 基本信息
        {
          path: 'basicInfo',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
          component: () => import('@/views/project-management/projectAndPermission/basicInfos/index.vue'),
          meta: {
            locale: 'project.permission.basicInfo',
            roles: ['SYSTEM_PARAMETER_SETTING_BASE:READ'],
          },
        },
        // 菜单管理
        {
          path: 'menuManagement',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
          component: () => import('@/views/project-management/projectAndPermission/menuManagement/menuManagement.vue'),
          meta: {
            locale: 'project.permission.menuManagement',
            roles: [
              'PROJECT_APPLICATION_WORKSTATION:READ',
              'PROJECT_APPLICATION_TEST_PLAN:READ',
              'PROJECT_APPLICATION_BUG:READ',
              'PROJECT_APPLICATION_CASE:READ',
              'PROJECT_APPLICATION_API:READ',
              'PROJECT_APPLICATION_UI:READ',
              'PROJECT_APPLICATION_PERFORMANCE_TEST:READ',
            ],
          },
        },
        // 项目版本
        {
          path: 'projectVersion',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION,
          component: () => import('@/views/project-management/projectAndPermission/projectVersion/index.vue'),
          meta: {
            locale: 'project.permission.projectVersion',
            roles: ['*'],
          },
        },
        // 成员
        {
          path: 'member',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
          component: () => import('@/views/project-management/projectAndPermission/member/index.vue'),
          meta: {
            locale: 'project.permission.member',
            roles: ['*'],
          },
        },
        // 用户组
        {
          path: 'projectUserGroup',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
          component: () => import('@/views/project-management/projectAndPermission/userGroup/projectUserGroup.vue'),
          meta: {
            locale: 'project.permission.userGroup',
            roles: ['PROJECT_GROUP:READ'],
          },
        },
      ],
    },
    // 項目管理模板
    {
      path: 'templateManager',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
      component: () => import('@/views/project-management/template/index.vue'),
      meta: {
        locale: 'menu.projectManagement.templateManager',
        roles: ['PROJECT_TEMPLATE:READ'],
        isTopMenu: true,
      },
    },
    // 模板列表-模板字段设置
    {
      path: 'filedSetting',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
      component: () => import('@/views/project-management/template/components/projectFieldSetting.vue'),
      meta: {
        locale: 'menu.settings.organization.templateFieldSetting',
        roles: ['*'],
        breadcrumbs: [
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
            locale: 'menu.projectManagement.templateManager',
          },
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
            locale: 'menu.settings.organization.templateFieldSetting',
            editLocale: 'menu.settings.organization.templateFieldSetting',
          },
        ],
      },
    },
    // 模板列表-模板管理列表
    {
      path: 'templateManagement',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
      component: () => import('@/views/project-management/template/components/templateManagement.vue'),
      meta: {
        locale: 'menu.settings.organization.templateManagement',
        roles: ['*'],
        breadcrumbs: [
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
            locale: 'menu.settings.organization.template',
          },
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
            locale: 'menu.settings.organization.templateManagementList',
            editLocale: 'menu.settings.organization.templateManagementList',
          },
        ],
      },
    },
    // 项目-模板-创建模板和模板详情
    {
      path: 'templateDetail/:mode?',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_DETAIL,
      component: () => import('@/views/project-management/template/components/proTemplateDetail.vue'),
      meta: {
        locale: 'menu.settings.organization.templateManagementDetail',
        roles: ['*'],
        breadcrumbs: [
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
            locale: 'menu.settings.organization.template',
          },
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
            locale: 'menu.settings.organization.templateManagementList',
          },
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_DETAIL,
            locale: 'menu.settings.organization.templateManagementDetail',
            editLocale: 'menu.settings.organization.templateManagementEdit',
          },
        ],
      },
    },
    // 模板列表-模板管理-工作流
    {
      path: 'templateWorkFlow',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_WORKFLOW,
      component: () => import('@/views/project-management/template/components/workFlowTableIndex.vue'),
      meta: {
        locale: 'menu.settings.organization.templateManagementWorkFlow',
        roles: ['*'],
        breadcrumbs: [
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
            locale: 'menu.settings.organization.bugTemplate',
          },
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_WORKFLOW,
            locale: 'menu.settings.organization.templateManagementWorkFlow',
          },
        ],
      },
    },
    // 文件管理
    {
      path: 'fileManagement',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_FILE_MANAGEMENT,
      component: () => import('@/views/project-management/fileManagement/index.vue'),
      meta: {
        locale: 'menu.projectManagement.fileManagement',
        roles: ['PROJECT_FILE_MANAGEMENT:READ'],
        isTopMenu: true,
      },
    },
    // 消息管理
    {
      path: 'messageManagement',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT,
      component: () => import('@/views/project-management/messageManagement/index.vue'),
      meta: {
        locale: 'menu.projectManagement.messageManagement',
        roles: ['PROJECT_MESSAGE:READ'],
        isTopMenu: true,
      },
    },
    {
      path: 'messageManagementEdit',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_EDIT,
      component: () => import('@/views/project-management/messageManagement/edit.vue'),
      meta: {
        locale: 'menu.projectManagement.messageManagementEdit',
        roles: ['*'],
        breadcrumbs: [
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT,
            locale: 'menu.projectManagement.messageManagement',
          },
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_EDIT,
            locale: 'menu.projectManagement.messageManagementEdit',
            editTag: 'id',
            editLocale: 'menu.projectManagement.messageManagementEdit',
          },
        ],
      },
    },
    // 公共脚本
    {
      path: 'commonScript',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_COMMON_SCRIPT,
      component: () => import('@/views/project-management/commonScript/index.vue'),
      meta: {
        locale: 'menu.projectManagement.commonScript',
        roles: ['PROJECT_CUSTOM_FUNCTION:READ'],
        isTopMenu: true,
      },
    },
    // 项目日志
    {
      path: 'log',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_LOG,
      component: () => import('@/views/project-management/log/index.vue'),
      meta: {
        locale: 'menu.projectManagement.log',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    // 菜单管理-误报规则
    {
      path: 'errorReportRule',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MENU_MANAGEMENT_ERROR_REPORT_RULE,
      component: () =>
        import('@/views/project-management/projectAndPermission/menuManagement/components/falseAlermRule.vue'),
      meta: {
        locale: 'project.menu.API_ERROR_REPORT_RULE',
        roles: ['PROJECT_APPLICATION_API:READ'],
        breadcrumbs: [
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
            locale: 'project.projectManagement.menuManagement',
          },
          {
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MENU_MANAGEMENT_ERROR_REPORT_RULE,
            locale: 'menu.projectManagement.menuManagementErrorReportRule',
          },
        ],
      },
    },
    // 环境管理
    {
      path: 'environmentManagement',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_ENVIRONMENT_MANAGEMENT,
      component: () => import('@/views/project-management/environmental/index.vue'),
      meta: {
        locale: 'menu.projectManagement.environmentManagement',
        roles: ['PROJECT_ENVIRONMENT:READ'],
        isTopMenu: true,
      },
    },
  ],
};

export default ProjectManagement;

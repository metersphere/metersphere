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
        roles: ['*'],
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
            roles: ['*'],
          },
        },
        // 菜单管理
        {
          path: 'menuManagement',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
          component: () => import('@/views/project-management/projectAndPermission/menuManagement/menuManagement.vue'),
          meta: {
            locale: 'project.permission.menuManagement',
            roles: ['*'],
          },
        },
        // 模版管理
        {
          path: 'templateManager',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_TEMPLATE,
          component: () => import('@/views/project-management/projectAndPermission/templateManagement/index.vue'),
          meta: {
            locale: 'project.permission.templateManager',
            roles: ['*'],
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
            roles: ['*'],
          },
        },
      ],
    },
    // 文件管理
    {
      path: 'fileManagement',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_FILE_MANAGEMENT,
      component: () => import('@/views/project-management/fileManagement/index.vue'),
      meta: {
        locale: 'menu.projectManagement.fileManagement',
        roles: ['*'],
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
        roles: ['*'],
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
  ],
};

export default ProjectManagement;

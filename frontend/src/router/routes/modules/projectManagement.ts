import { DEFAULT_LAYOUT } from '../base';
import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

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
    {
      path: 'index',
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_INDEX,
      component: () => import('@/views/project-management/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
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
          component: () => import('@/views/project-management/projectAndPermission/menuManagement/index.vue'),
          meta: {
            locale: 'project.permission.menuManagement',
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
            locale: 'menu.settings.system.member',
            roles: ['*'],
          },
        },
        // 用户组
        {
          path: 'projectUserGroup',
          name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
          component: () => import('@/views/project-management/projectAndPermission/userGroup/index.vue'),
          meta: {
            locale: 'project.permission.userGroup',
            roles: ['*'],
          },
        },
      ],
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

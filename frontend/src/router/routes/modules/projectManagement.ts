import { DEFAULT_LAYOUT } from '../base';
import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

import type { AppRouteRecordRaw } from '../types';

const ProjectManagement: AppRouteRecordRaw = {
  path: '/project-management',
  name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT,
  redirect: '/project-management/index',
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

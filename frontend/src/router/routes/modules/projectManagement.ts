import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const ProjectManagement: AppRouteRecordRaw = {
  path: '/project-management',
  name: menuEnum.PROJECTMANAGEMENT,
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
      name: 'ProjectManagementIndex',
      component: () => import('@/views/project-management/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default ProjectManagement;

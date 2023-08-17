import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const BugManagement: AppRouteRecordRaw = {
  path: '/bug-management',
  name: menuEnum.BUGMANAGEMENT,
  redirect: '/bug-management/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.bugManagement',
    icon: 'icon-icon_defect',
    order: 2,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'BugManagementIndex',
      component: () => import('@/views/bug-management/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default BugManagement;

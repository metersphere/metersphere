import { BugManagementRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const BugManagement: AppRouteRecordRaw = {
  path: '/bug-management',
  name: BugManagementRouteEnum.BUG_MANAGEMENT,
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
      name: 'bugManagementIndex',
      component: () => import('@/views/bug-management/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default BugManagement;

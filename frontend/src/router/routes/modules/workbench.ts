import { DEFAULT_LAYOUT } from '../base';
import { WorkbenchRouteEnum } from '@/enums/routeEnum';

import type { AppRouteRecordRaw } from '../types';

const Workbench: AppRouteRecordRaw = {
  path: '/workbench',
  name: WorkbenchRouteEnum.WORKBENCH,
  redirect: '/workbench/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.workbench',
    icon: 'icon-icon_pc_filled',
    order: 0,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'WorkbenchIndex',
      component: () => import('@/views/workbench/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default Workbench;

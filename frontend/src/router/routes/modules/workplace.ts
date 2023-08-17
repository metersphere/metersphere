import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const WorkPlace: AppRouteRecordRaw = {
  path: '/workplace',
  name: menuEnum.WORKPLACE,
  redirect: '/workplace/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.workplace',
    icon: 'icon-icon_pc_filled',
    order: 0,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'WorkPlaceIndex',
      component: () => import('@/views/workplace/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default WorkPlace;

import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const components: AppRouteRecordRaw = {
  path: '/component',
  name: 'component',
  component: DEFAULT_LAYOUT,
  redirect: '/component/index',
  meta: {
    locale: 'menu.component.demo',
    icon: 'icon-computer',
    order: 1,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'component',
      component: () => import('@/views/component/index.vue'),
      meta: {
        locale: 'menu.component.demo',
        roles: ['*'],
      },
    },
  ],
};
export default components;

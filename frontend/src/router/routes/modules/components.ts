import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const components: AppRouteRecordRaw = {
  path: '/component',
  name: 'component',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.component',
    roles: ['*'],
    order: 1,
    icon: 'icon-common',
  },
  children: [
    {
      path: 'form-create',
      name: 'form-create',
      component: () => import('@/views/component/index.vue'),
      meta: {
        locale: 'menu.component.demo',
        roles: ['*'],
        icon: 'icon-computer',
      },
    },
  ],
};
export default components;

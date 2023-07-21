import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const FeatureTest: AppRouteRecordRaw = {
  path: '/feature-test',
  name: menuEnum.FEATURETEST,
  redirect: '/feature-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.featureTest',
    icon: 'icon_functional_testing',
    order: 3,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'FeatureTestIndex',
      component: () => import('@/views/feature-test/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default FeatureTest;

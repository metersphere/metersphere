import { DEFAULT_LAYOUT } from '../base';
import { FeatureTestRouteEnum } from '@/enums/routeEnum';

import type { AppRouteRecordRaw } from '../types';

const FeatureTest: AppRouteRecordRaw = {
  path: '/feature-test',
  name: FeatureTestRouteEnum.FEATURE_TEST,
  redirect: '/feature-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.featureTest',
    icon: 'icon-icon_functional_testing',
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

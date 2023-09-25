import { DEFAULT_LAYOUT } from '../base';
import { FeatureTestRouteEnum } from '@/enums/routeEnum';

import type { AppRouteRecordRaw } from '../types';

const FeatureTest: AppRouteRecordRaw = {
  path: '/feature-test',
  name: FeatureTestRouteEnum.FEATURE_TEST,
  redirect: '/feature-test/featureCase',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.featureTest',
    icon: 'icon-icon_functional_testing',
    order: 3,
    hideChildrenInMenu: true,
  },
  children: [
    // 功能用例
    {
      path: 'featureCase',
      name: FeatureTestRouteEnum.FEATURE_TEST_CASE,
      component: () => import('@/views/feature-test/featureCase/index.vue'),
      meta: {
        locale: 'menu.featureTest.featureCase',
        roles: ['*'],
        isTopMenu: true,
      },
    },
  ],
};

export default FeatureTest;

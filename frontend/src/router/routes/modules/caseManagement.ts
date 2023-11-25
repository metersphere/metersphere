import { FeatureTestRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
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
      component: () => import('@/views/case-management/caseManagementFeature/index.vue'),
      meta: {
        locale: 'menu.featureTest.featureCase',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    // 功能用例回收站
    {
      path: 'featureCaseRecycle',
      name: FeatureTestRouteEnum.FEATURE_TEST_CASE_RECYCLE,
      component: () => import('@/views/case-management/caseManagementFeature/components/recycleCaseTable.vue'),
      meta: {
        locale: 'menu.featureTest.featureCaseRecycle',
        roles: ['*'],
        breadcrumbs: [
          {
            name: FeatureTestRouteEnum.FEATURE_TEST_CASE,
            locale: 'menu.featureTest.featureCaseList',
          },
          {
            name: FeatureTestRouteEnum.FEATURE_TEST_CASE_RECYCLE,
            locale: 'menu.featureTest.featureCaseRecycle',
          },
        ],
      },
    },
    // 创建用例&编辑用例
    {
      path: 'featureCaseDetail',
      name: FeatureTestRouteEnum.FEATURE_TEST_CASE_DETAIL,
      component: () => import('@/views/case-management/caseManagementFeature/components/caseDetail.vue'),
      meta: {
        locale: 'menu.featureTest.featureCaseDetail',
        roles: ['*'],
        breadcrumbs: [
          {
            name: FeatureTestRouteEnum.FEATURE_TEST_CASE,
            locale: 'menu.featureTest.featureCase',
          },
          {
            name: FeatureTestRouteEnum.FEATURE_TEST_CASE_DETAIL,
            locale: 'menu.featureTest.featureCaseDetail',
          },
        ],
      },
    },
  ],
};

export default FeatureTest;

import { ShareEnum } from '@/enums/routeEnum';

import { SHARE_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const ShareRoute: AppRouteRecordRaw = {
  path: '/share',
  name: ShareEnum.SHARE,
  component: SHARE_LAYOUT,
  meta: {
    hideInMenu: true,
    roles: ['*'],
    requiresAuth: true,
  },
  children: [
    // 接口测试-场景报告-详情
    {
      path: 'shareReportScenario',
      name: ShareEnum.SHARE_REPORT_SCENARIO,
      component: () => import('@/views/api-test/report/shareSceneIndex.vue'),
      meta: {
        locale: '',
        roles: ['*'],
        isTopMenu: false,
      },
    },
    // 接口测试-用例报告-详情
    {
      path: 'shareReportCase',
      name: ShareEnum.SHARE_REPORT_CASE,
      component: () => import('@/views/api-test/report/shareCaseIndex.vue'),
      meta: {
        locale: '',
        roles: ['*'],
        isTopMenu: false,
      },
    },
    // 测试计划-报告-详情
    {
      path: 'shareReportTestPlan',
      name: ShareEnum.SHARE_REPORT_TEST_PLAN,
      component: () => import('@/views/test-plan/report/detail/sharePlanReportIndex.vue'),
      meta: {
        locale: '',
        roles: ['*'],
        isTopMenu: false,
      },
    },
  ],
};

export default ShareRoute;

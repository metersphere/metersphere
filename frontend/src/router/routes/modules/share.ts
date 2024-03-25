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
    // 测试计划
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
  ],
};

export default ShareRoute;

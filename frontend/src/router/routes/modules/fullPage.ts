import { FullPageEnum } from '@/enums/routeEnum';

import { FULL_PAGE_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const FullPage: AppRouteRecordRaw = {
  path: '/fullPage',
  name: FullPageEnum.FULL_PAGE,
  redirect: '/fullPage/testPlanExportPDF',
  component: FULL_PAGE_LAYOUT,
  children: [
    {
      path: 'testPlanExportPDF',
      name: FullPageEnum.FULL_PAGE_TEST_PLAN_EXPORT_PDF,
      component: () => import('@/views/test-plan/report/detail/exportPDF.vue'),
    },
  ],
};

export default FullPage;

import { FullPageEnum } from '@/enums/routeEnum';

import { FULL_PAGE_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const FullPage: AppRouteRecordRaw = {
  path: '/fullPage',
  name: FullPageEnum.FULL_PAGE,
  redirect: '/fullPage/testPlanExportPDF',
  component: FULL_PAGE_LAYOUT,
  meta: {
    hideInMenu: true,
  },
  children: [
    {
      path: 'testPlanExportPDF',
      name: FullPageEnum.FULL_PAGE_TEST_PLAN_EXPORT_PDF,
      component: () => import('@/views/test-plan/report/detail/exportPDF.vue'),
    },
    {
      path: 'scenarioExportPDF',
      name: FullPageEnum.FULL_PAGE_SCENARIO_EXPORT_PDF,
      component: () => import('@/views/api-test/report/exportScenarioPDF.vue'),
    },
    {
      path: 'apiCaseExportPDF',
      name: FullPageEnum.FULL_PAGE_API_CASE_EXPORT_PDF,
      component: () => import('@/views/api-test/report/exportCasePDF.vue'),
    },
  ],
};

export default FullPage;

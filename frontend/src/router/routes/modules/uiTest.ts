import { UITestRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const UiTest: AppRouteRecordRaw = {
  path: '/ui-test',
  name: UITestRouteEnum.UI_TEST,
  redirect: '/ui-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.uiTest',
    icon: 'icon-icon_ui-test-filled',
    order: 5,
    hideChildrenInMenu: true,
    roles: ['UI_INDEX:READ'],
  },
  children: [
    {
      path: 'index',
      name: 'uiTestIndex',
      component: () => import('@/views/ui-test/index.vue'),
      meta: {
        roles: ['UI_INDEX:READ'],
      },
    },
  ],
};

export default UiTest;

import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';
import menuEnum from '@/enums/menuEnum';

const UiTest: AppRouteRecordRaw = {
  path: '/ui-test',
  name: menuEnum.UITEST,
  redirect: '/ui-test/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.uiTest',
    icon: 'icon-icon_ui-test-filled',
    order: 5,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'UiTestIndex',
      component: () => import('@/views/ui-test/index.vue'),
      meta: {
        roles: ['*'],
      },
    },
  ],
};

export default UiTest;

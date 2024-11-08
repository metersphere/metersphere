import { WorkbenchRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const TestPlan: AppRouteRecordRaw = {
  path: '/workstation',
  name: WorkbenchRouteEnum.WORKBENCH,
  redirect: '/workstation/home',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.workbench',
    collapsedLocale: 'menu.workbenchHomeSort',
    icon: 'icon-a-icon_test-tracking_filled1',
    order: 0,
    hideChildrenInMenu: true,
    roles: ['*'],
  },
  children: [
    // 首页
    {
      path: 'home',
      name: WorkbenchRouteEnum.WORKBENCH_INDEX,
      component: () => import('@/views/workbench/homePage/index.vue'),
      meta: {
        locale: 'menu.workbenchHomeSort',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    // 待办
    {
      path: 'wait',
      name: WorkbenchRouteEnum.WORKBENCH_INDEX_WAIT,
      component: () => import('@/views/workbench/myToDo/index.vue'),
      meta: {
        locale: 'menu.workbenchWaitSort',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    // 我关注的
    {
      path: 'followed',
      name: WorkbenchRouteEnum.WORKBENCH_INDEX_FOLLOW,
      component: () => import('@/views/workbench/myFollowed/index.vue'),
      meta: {
        locale: 'menu.workbenchFollowSort',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    // 我创建的
    {
      path: 'created',
      name: WorkbenchRouteEnum.WORKBENCH_INDEX_CREATED,
      component: () => import('@/views/workbench/myCreated/index.vue'),
      meta: {
        locale: 'menu.workbenchCreatedSort',
        roles: ['*'],
        isTopMenu: true,
      },
    },
  ],
};

export default TestPlan;

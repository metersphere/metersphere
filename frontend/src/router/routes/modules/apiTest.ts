import { ApiTestRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const ApiTest: AppRouteRecordRaw = {
  path: '/api-test',
  name: ApiTestRouteEnum.API_TEST,
  redirect: '/api-test/debug',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.apiTest',
    icon: 'icon-icon_api-test-filled',
    order: 4,
    hideChildrenInMenu: true,
    roles: [
      'PROJECT_API_DEBUG:READ',
      'PROJECT_API_DEFINITION:READ',
      'PROJECT_API_DEFINITION_CASE:READ',
      'PROJECT_API_DEFINITION_MOCK:READ',
      'PROJECT_API_SCENARIO:READ',
      'PROJECT_API_REPORT:READ',
    ],
  },
  children: [
    {
      path: 'debug',
      name: ApiTestRouteEnum.API_TEST_DEBUG_MANAGEMENT,
      component: () => import('@/views/api-test/debug/index.vue'),
      meta: {
        locale: 'menu.apiTest.debug',
        roles: ['PROJECT_API_DEBUG:READ'],
        isTopMenu: true,
      },
    },
    {
      path: 'management',
      name: ApiTestRouteEnum.API_TEST_MANAGEMENT,
      component: () => import('@/views/api-test/management/index.vue'),
      meta: {
        locale: 'menu.apiTest.management',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    // 接口定义回收站
    {
      path: 'recycle',
      name: ApiTestRouteEnum.API_TEST_MANAGEMENT_RECYCLE,
      component: () => import('@/views/api-test/management/recycle.vue'),
      meta: {
        locale: 'common.recycle',
        roles: ['FUNCTIONAL_CASE:READ'],
        breadcrumbs: [
          {
            name: ApiTestRouteEnum.API_TEST_MANAGEMENT,
            locale: 'menu.apiTest.api',
          },
          {
            name: ApiTestRouteEnum.API_TEST_MANAGEMENT_RECYCLE,
            locale: 'common.recycle',
          },
        ],
      },
    },
    {
      path: 'scenario',
      name: ApiTestRouteEnum.API_TEST_SCENARIO,
      component: () => import('@/views/api-test/scenario/index.vue'),
      meta: {
        locale: 'menu.apiTest.scenario',
        roles: ['*'],
        isTopMenu: true,
      },
    },

    {
      path: 'scenarioRecycle',
      name: ApiTestRouteEnum.API_TEST_SCENARIO_RECYCLE,
      component: () => import('@/views/api-test/scenario/index.vue'),
      meta: {
        locale: 'menu.apiTest.scenario',
        roles: ['*'],
        isTopMenu: false,
      },
    },

    {
      path: 'report',
      name: ApiTestRouteEnum.API_TEST_REPORT,
      component: () => import('@/views/api-test/report/index.vue'),
      meta: {
        locale: 'menu.apiTest.report',
        roles: ['*'],
        isTopMenu: true,
      },
    },
  ],
};

export default ApiTest;

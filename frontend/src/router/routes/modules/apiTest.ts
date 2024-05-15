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
    collapsedLocale: 'menu.apiTestShort',
    icon: 'icon-icon_api-test-filled2',
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
        roles: ['PROJECT_API_DEFINITION:READ'],
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
        roles: ['PROJECT_API_DEFINITION:READ'],
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
        isTopMenu: true,
        roles: ['PROJECT_API_SCENARIO:READ'],
      },
    },

    // 接口场景回收站
    {
      path: 'scenario/recycle',
      name: ApiTestRouteEnum.API_TEST_SCENARIO_RECYCLE,
      component: () => import('@/views/api-test/scenario/recycle.vue'),
      meta: {
        locale: 'menu.apiTest.scenario',
        roles: ['PROJECT_API_SCENARIO:READ'],
        isTopMenu: false,
        breadcrumbs: [
          {
            name: ApiTestRouteEnum.API_TEST_SCENARIO,
            locale: 'menu.apiTest.apiScenario',
          },
          {
            name: ApiTestRouteEnum.API_TEST_SCENARIO_RECYCLE,
            locale: 'common.recycle',
          },
        ],
      },
    },

    {
      path: 'report',
      name: ApiTestRouteEnum.API_TEST_REPORT,
      component: () => import('@/views/api-test/report/index.vue'),
      meta: {
        locale: 'menu.apiTest.report',
        roles: ['PROJECT_API_REPORT:READ'],
        isTopMenu: true,
      },
    },
  ],
};

export default ApiTest;

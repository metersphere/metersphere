import {
  ApiTestRouteEnum,
  BugManagementRouteEnum,
  CaseManagementRouteEnum,
  TestPlanRouteEnum,
} from '@/enums/routeEnum';

// 路由白名单，无需校验权限与登录状态
export const WHITE_LIST = [
  { name: 'notFound', path: '/notFound', children: [] },
  { name: 'invite', path: '/invite', children: [] },
  { name: 'index', path: '/index', children: [] },
  {
    name: 'share',
    path: '/share',
    children: [
      {
        name: 'shareReportScenario',
        path: '/shareReportScenario',
      },
      {
        name: 'shareReportCase',
        path: '/shareReportCase',
        children: [],
      },
      {
        name: 'shareReportTestPlan',
        path: '/shareReportTestPlan',
        children: [],
      },
      {
        name: 'shareDefinitionApi',
        path: '/shareDefinitionApi',
        children: [],
      },
    ],
  },
  {
    name: 'shareReportScenario',
    path: '/shareReportScenario',
    children: [],
  },
  {
    name: 'shareReportCase',
    path: '/shareReportCase',
    children: [],
  },
  {
    name: 'shareReportTestPlan',
    path: '/shareReportTestPlan',
    children: [],
  },
  {
    name: 'shareDefinitionApi',
    path: '/shareDefinitionApi',
    children: [],
  },
];

// 左侧菜单底部对齐的菜单数组，数组项为一级路由的name
export const BOTTOM_MENU_LIST = ['setting'];

// 404 路由
export const NOT_FOUND = {
  name: 'notFound',
};

// 重定向中转站路由
export const REDIRECT_ROUTE_NAME = 'Redirect';

// 首页路由
export const DEFAULT_ROUTE_NAME = 'workbench';

// 无资源/权限路由
export const NO_RESOURCE_ROUTE_NAME = 'no-resource';

// 无项目路由
export const NO_PROJECT_ROUTE_NAME = 'no-project';

// 白板用户首页
export const WHITEBOARD_INDEX = 'index';

export const WHITE_LIST_NAME = WHITE_LIST.map((el) => el.name);

// 全屏无资源页面用于分享全屏的页面
export const NOT_FOUND_RESOURCE = 'notResourceScreen';

// 功能路由映射
export const featureRouteMap: Record<string, any> = {
  [ApiTestRouteEnum.API_TEST]: 'apiTest',
  [CaseManagementRouteEnum.CASE_MANAGEMENT]: 'caseManagement',
  [TestPlanRouteEnum.TEST_PLAN]: 'testPlan',
  [BugManagementRouteEnum.BUG_MANAGEMENT]: 'bugManagement',
};

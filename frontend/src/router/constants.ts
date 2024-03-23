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

// 路由白名单，无需校验权限与登录状态
export const WHITE_LIST = [
  { name: 'notFound', children: [] },
  { name: 'login', children: [] },
  { name: 'invite', children: [] },
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
export const DEFAULT_ROUTE_NAME = 'Workplace';

// 默认 tab-bar 路，多页签模式下，打开的第一个页面
export const DEFAULT_ROUTE = {
  title: 'menu.dashboard.workplace',
  name: DEFAULT_ROUTE_NAME,
  fullPath: '/dashboard/workplace',
};

// 不需要显示项目选择器的模块，数组项为一级路由的path
export const NOT_SHOW_PROJECT_SELECT_MODULE = ['setting'];

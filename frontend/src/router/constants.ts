// 路由白名单，无需校验权限与登录状态
export const WHITE_LIST = [
  { name: 'notFound', children: [] },
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
export const DEFAULT_ROUTE_NAME = 'Workbench';

import Vue from 'vue';
import Router from 'vue-router';
import API from '@/router/modules/api';

// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch((error) => error);
};

Vue.use(Router);

// project 顶部菜单
API.children.forEach((item) => {
  item.children = [{ path: '', component: item.component }];
  item.component = () => import('@/business/ApiTest');
});

export const constantRoutes = [
  { path: '/', redirect: '/api/home' },
  {
    path: '/login',
    component: () => import('metersphere-frontend/src/business/login'),
    hidden: true,
  },
  API,
];

export const microRoutes = [
  {
    path: '/automation/report/view/:reportId',
    name: 'ApiScenarioReportView',
    component: () => import('@/business/automation/report/ApiReportView'),
  },
  {
    path: '/definition/report/view/:reportId',
    name: 'ApiReportView',
    component: () =>
      import('@/business/definition/components/response/ApiResponseView'),
  },
];

const createRouter = () =>
  new Router({
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes,
  });

const router = createRouter();

export default router;

export const microRouter = new Router({
  mode: 'abstract',
  scrollBehavior: () => ({ y: 0 }),
  routes: microRoutes,
});

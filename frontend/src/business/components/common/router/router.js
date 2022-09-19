import Vue from "vue";
import VueRouter from 'vue-router';
import RouterSidebar from "./RouterSidebar";
import Setting from "@/business/components/settings/router";
import API from "@/business/components/api/router";
import Performance from "@/business/components/performance/router";
import Track from "@/business/components/track/router";
import ReportStatistics from "@/business/components/reportstatistics/router";
import Project from "@/business/components/project/router";
import {getCurrentUser, getCurrentUserId, hasPermissions} from "@/common/js/utils";
import {SECOND_LEVEL_ROUTE_PERMISSION_MAP} from "@/common/js/constants";

Vue.use(VueRouter);
const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/);
const router = new VueRouter({
  routes: [
    {path: "/", redirect: '/setting/personsetting'},
    {
      path: "/sidebar",
      components: {
        sidebar: RouterSidebar
      }
    },
    ...requireContext.keys().map(k => requireContext(k).workstation),
    ...requireContext.keys().map(k => requireContext(k).ui),
    Setting,
    API,
    Performance,
    Track,
    ReportStatistics,
    Project
  ]
});

router.beforeEach((to, from, next) => {
  if (getCurrentUser() && getCurrentUser().id) {
    redirectLoginPath(to.fullPath, next);
  } else {
    next();
  }
});

//重复点击导航路由报错
const routerPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error);
};


// 登入后跳转至原路径
function redirectLoginPath(originPath, next) {
  let redirectUrl = sessionStorage.getItem('redirectUrl');
  let loginSuccess = sessionStorage.getItem('loginSuccess');

  if (!redirectUrl || redirectUrl === '/') {
    if (hasPermissions('PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_TEMPLATE:READ', 'PROJECT_MESSAGE:READ')) {
      redirectUrl = '/project/home';
    } else if (hasPermissions('WORKSPACE_SERVICE:READ', 'PROJECT_MESSAGE:READ', 'WORKSPACE_USER:READ', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ')) {
      redirectUrl = '/setting/project/:type';
    } else if (hasPermissions('SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ', 'SYSTEM_QUOTA:READ', 'SYSTEM_OPERATING_LOG:READ')) {
      redirectUrl = '/setting';
    } else {
      redirectUrl = '/';
    }
  }

  sessionStorage.setItem('lastUser', getCurrentUserId());
  sessionStorage.setItem('redirectUrl', originPath);
  sessionStorage.removeItem('loginSuccess');
  let defaultMenuRoute = sessionStorage.getItem('defaultMenuRoute');

  if (redirectUrl && loginSuccess) {
    // 登录后只执行一次
    sessionStorage.removeItem('loginSuccess');
    redirectUrl = getDefaultSecondLevelMenu(redirectUrl);
    next({path: redirectUrl});
  } else {
    if (!defaultMenuRoute) {
      // 记录标识，防止死循环
      sessionStorage.setItem('defaultMenuRoute', 'sign');
      let changedPath = getDefaultSecondLevelMenu(originPath);
      if (changedPath === originPath) {
        // 通过了权限校验，保留路由相关信息，直接放行
        next();
      } else {
        // 未通过校验，放行至有权限路由
        next({path: changedPath});
      }
      if (router.currentRoute.fullPath === changedPath) {
        sessionStorage.setItem('redirectUrl', changedPath);
        // 路径相同时，移除标识
        sessionStorage.removeItem("defaultMenuRoute");
      }
    } else {
      sessionStorage.setItem('redirectUrl', originPath);
      sessionStorage.removeItem("defaultMenuRoute");
      next();
    }
  }
}

export function getDefaultSecondLevelMenu(toPath) {
  let {TRACK: tracks, API: apis, LOAD: loads, UI: ui, REPORT: report} = SECOND_LEVEL_ROUTE_PERMISSION_MAP;
  if (tracks.map(r => r.router).indexOf(toPath) > -1) {
    return _getDefaultSecondLevelMenu(tracks, toPath);
  } else if (apis.map(r => r.router).indexOf(toPath) > -1) {
    return _getDefaultSecondLevelMenu(apis, toPath);
  } else if (loads.map(r => r.router).indexOf(toPath) > -1) {
    return _getDefaultSecondLevelMenu(loads, toPath);
  } else if (ui.map(r => r.router).indexOf(toPath) > -1) {
    return _getDefaultSecondLevelMenu(ui, toPath);
  } else if (report.map(r => r.router).indexOf(toPath) > -1) {
    return _getDefaultSecondLevelMenu(report, toPath);
  } else {
    return toPath;
  }
}

function _getDefaultSecondLevelMenu(secondLevelRouters, toPath) {
  let toRouter = secondLevelRouters.find(r => r['router'] === toPath);
  if (toRouter && hasPermissions(...toRouter['permission'])) {
    // 将要跳转的路由有权限则放行
    return toPath;
  }
  for (let router of secondLevelRouters) {
    if (hasPermissions(...router['permission'])) {
      // 返回第一个有权限的路由路径
      return router['router'];
    }
  }
  return '/';
}


export default router;

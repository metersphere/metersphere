import Vue from "vue"
import Router from "vue-router"
import Layout from "../business/app-layout"
import {getCurrentUserId} from "../utils/token";
import {hasPermissions} from "../utils/permission";
import {SECOND_LEVEL_ROUTE_PERMISSION_MAP} from "../utils/constants";

// 加载modules中的路由
const modules = require.context("./modules", true, /\.js$/)

// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error)
}

Vue.use(Router)

export const constantRoutes = [
  {
    path: "/redirect",
    component: Layout,
    hidden: true,
    children: [
      {
        path: "/redirect/:path(.*)",
        component: () => import("../components/redirect")
      }
    ]
  },
  {
    path: "/login",
    component: () => import("../business/login"),
    hidden: true
  },
  {
    path: "/",
    component: Layout,
    redirect: "/setting/personsetting",
  }
]

/**
 * 用户登录后根据角色加载的路由
 */
export const rolesRoutes = [
  // 先按sort排序
  ...modules.keys().map(key => modules(key).default).sort((r1, r2) => {
    if (!r1.sort) r1.sort = Number.MAX_VALUE
    if (!r2.sort) r2.sort = Number.MAX_VALUE
    return r1.sort - r2.sort
  }),
  {path: "*", redirect: "/", hidden: true}
]

const createRouter = () => new Router({
  scrollBehavior: () => ({y: 0}),
  routes: constantRoutes
})

const router = createRouter()

let store = null;
router.beforeEach(async (to, from, next) => {
  if (store === null) {
    const {useUserStore} = await import('@/store');
    store = useUserStore()
  }
  let user = store.currentUser
  if (to.path.split('/')[1] !== from.path.split('/')[1]) {
    try {
      user = await store.getIsLogin();
    } catch (e) {
      // console.error(e);
    }
  }
  if (user && user.id) {
    redirectLoginPath(to.fullPath, next);
  } else {
    next();
  }
});

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
    let changedPath = getDefaultSecondLevelMenu(originPath);
    if (changedPath === originPath) {
      // 通过了权限校验，保留路由相关信息，直接放行
      next();
    } else {
      // 未通过校验，放行至有权限路由
      next({path: changedPath});
    }
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
      if (router.currentRoute.fullPath === originPath) {
        sessionStorage.setItem('redirectUrl', originPath);
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

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router

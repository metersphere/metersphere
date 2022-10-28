import Vue from "vue"
import Router from "vue-router"
import Layout from "../business/app-layout"
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

let userStore = null;
// 刷新整个页面会到这里
import('@/store').then(async ({useUserStore}) => {
  userStore = useUserStore();
  await userStore.getIsLogin()
});

let store = null;
router.beforeEach(async (to, from, next) => {
  if (store === null) {
    const {useUserStore} = await import('@/store');
    store = useUserStore();
  }
  let formModule = from.path.split('/')[1];
  let toModule = to.path.split('/')[1];
  if (to.path !== '/login' && formModule && toModule !== formModule) {
    try {
      await store.getIsLogin();
    } catch (e) {
      // nothing
    }
  }

  if (to.name === "login_redirect" || to.path === "/login") {
    next();
    return;
  }

  // 二级菜单权限控制
  let changedPath = getDefaultSecondLevelMenu(to.fullPath);
  sessionStorage.setItem('redirectUrl', changedPath);
  if (changedPath === to.fullPath) {
    // 有权限则放行
    next();
  } else {
    // 未通过校验，放行至有权限路由
    next({path: changedPath});
  }
});

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

import Vue from "vue";
import VueRouter from 'vue-router';
import RouterSidebar from "./RouterSidebar";
import Setting from "@/business/components/settings/router";
import API from "@/business/components/api/router";
import Performance from "@/business/components/performance/router";
import Track from "@/business/components/track/router";
import ReportStatistics from "@/business/components/reportstatistics/router";
import Project from "@/business/components/project/router";
import {getCurrentUserId, hasPermissions} from "@/common/js/utils";

Vue.use(VueRouter);
const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/);
const router = new VueRouter({
  routes: [
    {path: "/", redirect: '/setting'},
    {
      path: "/sidebar",
      components: {
        sidebar: RouterSidebar
      }
    },
    ...requireContext.keys().map(key => requireContext(key).workstation),
    Setting,
    API,
    Performance,
    Track,
    ReportStatistics,
    Project
  ]
});

router.beforeEach((to, from, next) => {

  redirectLoginPath(to.fullPath);

  //解决localStorage清空，cookie没失效导致的卡死问题
  if (!localStorage.getItem('Admin-Token')) {
    // axios.get("/signout");
    // console.log("signout");
    localStorage.setItem('Admin-Token', "{}");
    window.location.href = "/login";
    next();
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
function redirectLoginPath(originPath) {
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

  if (redirectUrl && loginSuccess) {
    sessionStorage.removeItem('loginSuccess');
    router.push(redirectUrl);
  }
  sessionStorage.setItem('lastUser', getCurrentUserId());
  sessionStorage.setItem('redirectUrl', originPath);
  sessionStorage.removeItem('loginSuccess');
}


export default router;

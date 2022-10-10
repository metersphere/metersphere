import Vue from "vue"
import Router from "vue-router"
import Report from "@/router/modules/report";


// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error)
}

Vue.use(Router)

//  顶部菜单
Report.children.forEach(item => {
  item.children = [{path: '', component: item.component}];
  item.component = () => import('@/business/ReportStatistics')
})

export const constantRoutes = [
  {path: "/", redirect: "/report/project-statistics"},
  {
    path: "/login",
    component: () => import("metersphere-frontend/src/business/login"),
    hidden: true
  },
  Report
]

const createRouter = () => new Router({
  scrollBehavior: () => ({y: 0}),
  routes: constantRoutes
})

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

const router = createRouter()


export default router

import Vue from "vue"
import Router from "vue-router"
import Performance from "@/router/modules/performance";


// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error)
}

Vue.use(Router)

// project 顶部菜单
Performance.children.forEach(item => {
  item.children = [{path: '', component: item.component}];
  item.component = () => import('@/business/PerformanceTest')
})

export const constantRoutes = [
  {path: "/", redirect: "/performance/home"},
  {
    path: "/login",
    component: () => import("metersphere-frontend/src/business/login"),
    hidden: true
  },
  Performance
]

export const microRoutes = [
  {
    path: '/performance/report/view/:reportId',
    name: "perReportView",
    component: () => import('@/business/report/PerformanceReportView'),
  },
  {
    path: '/share/performance/report/view/:reportId',
    name: "sharePerReportView",
    component: () => import('@/template/report/performance/share/ShareLoadReportView'),
  }
]

const createRouter = () => new Router({
  scrollBehavior: () => ({y: 0}),
  routes: constantRoutes
})

const router = createRouter()

export default router

export const microRouter = new Router({
  mode: 'abstract',
  scrollBehavior: () => ({y: 0}),
  routes: microRoutes
})


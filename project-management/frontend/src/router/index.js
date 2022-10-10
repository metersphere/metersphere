import Vue from "vue"
import Router from "vue-router"
import Project from "@/router/modules/project";


// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error)
}

Vue.use(Router)

// project 顶部菜单
Project.children.forEach(item => {
  item.children = [{path: '', component: item.component}];
  item.component = () => import('@/business/ProjectSetting')
})

export const constantRoutes = [
  {path: "/", redirect: "/project/home"},
  {
    path: "/login",
    component: () => import("metersphere-frontend/src/business/login"),
    hidden: true
  },
  Project
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

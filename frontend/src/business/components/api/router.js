import MsProject from "@/business/components/project/MsProject";

export default {
  path: "/api",
  name: "api",
  redirect: "/api/home",
  components: {
    content: () => import(/* webpackChunkName: "api" */ '@/business/components/api/ApiTest')
  },
  children: [
    {
      path: 'home',
      name: 'fucHome',
      component: () => import(/* webpackChunkName: "api" */ '@/business/components/api/home/ApiTestHome'),
    },
    {
      path: "test/:type",
      name: "ApiTestConfig",
      component: () => import(/* webpackChunkName: "api" */ '@/business/components/api/test/ApiTestConfig'),
      props: (route) => ({id: route.query.id})
    },
    {
      path: "test/list/:projectId",
      name: "ApiTestList",
      component: () => import(/* webpackChunkName: "api" */ '@/business/components/api/test/ApiTestList'),
    },
    {
      path: "project/:type",
      name: "fucProject",
      component: MsProject,
    },
    {
      path: "report/list/:testId",
      name: "ApiReportList",
      component: () => import(/* webpackChunkName: "api" */ '@/business/components/api/report/ApiReportList'),
    },
    {
      path: "report/view/:reportId",
      name: "ApiReportView",
      component: () => import(/* webpackChunkName: "api" */ '@/business/components/api/report/ApiReportView'),
    }
  ]
}

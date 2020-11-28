import MsProject from "@/business/components/project/MsProject";

export default {
  path: "/api",
  name: "api",
  redirect: "/api/home",
  components: {
    content: () => import('@/business/components/api/ApiTest')
  },
  children: [
    {
      path: 'home',
      name: 'fucHome',
      component: () => import('@/business/components/api/home/ApiTestHome'),
    },
    {
      path: "test/:type",
      name: "ApiTestConfig",
      component: () => import('@/business/components/api/test/ApiTestConfig'),
      props: (route) => ({id: route.query.id})
    },
    {
      path: "test/list/:projectId",
      name: "ApiTestList",
      component: () => import('@/business/components/api/test/ApiTestList'),
    },
    {
      path: "project/:type",
      name: "fucProject",
      component: MsProject,
    },
    {
      path: "report/list/:testId",
      name: "ApiReportList",
      component: () => import('@/business/components/api/report/ApiReportList'),
    },
    {
      path: "report/view/:reportId",
      name: "ApiReportView",
      component: () => import('@/business/components/api/report/ApiReportView'),
    },
    {
      path: "definition",
      name: "ApiDefinition",
      component: () => import('@/business/components/api/definition/ApiDefinition'),
    },
    {
      path: 'monitor/view',
      name: 'ApiMonitor',
      component: () => import('@/business/components/api/monitor/ApiMonitor'),
    },
  ]
}

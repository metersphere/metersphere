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
      component: () => import('@/business/components/api/homepage/ApiTestHomePage'),
    },
    // {
    //   path: "project/:type",
    //   name: "fucProject",
    //   component: MsProject,
    // },
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
      path: "automation/report",
      name: "ApiReportList",
      component: () => import('@/business/components/api/automation/report/ApiReportList'),
    },
    {
      path: "automation/report/view/:reportId",
      name: "ApiReportView",
      component: () => import('@/business/components/api/automation/report/ApiReportView'),

    },
    {
      path: "definition/:redirectID?/:dataType?/:dataSelectRange?",
      name: "ApiDefinition",
      component: () => import('@/business/components/api/definition/ApiDefinition'),
    },
    {
      path: "automation/:redirectID?/:dataType?/:dataSelectRange?",
      name: "ApiAutomation",
      component: () => import('@/business/components/api/automation/ApiAutomation'),
    },
    {
      path: 'monitor/view',
      name: 'ApiMonitor',
      component: () => import('@/business/components/api/monitor/ApiMonitor'),
    },
  ]
};

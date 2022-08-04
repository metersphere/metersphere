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
    {
      path: "report/view/:reportId",
      name: "ApiReportViews",
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
      path: "definition",
      name: "ApiDefinition",
      component: () => import('@/business/components/api/definition/ApiDefinition'),
    },
    {
      path: "definition/:redirectID?/:dataType?/:dataSelectRange?/:projectId?/:type?/:workspaceId?",
      name: "ApiDefinitionWithQuery",
      component: () => import('@/business/components/api/definition/ApiDefinition'),
    },
    {
      path: "automation/:redirectID?/:dataType?/:dataSelectRange?/:projectId?/:workspaceId?",
      name: "ApiAutomationWithQuery",
      component: () => import('@/business/components/api/automation/ApiAutomation'),
    },
    {
      path: "automation",
      name: "ApiAutomation",
      component: () => import('@/business/components/api/automation/ApiAutomation'),
    },
    {
      path: 'monitor/view',
      name: 'ApiMonitor',
      component: () => import('@/business/components/api/monitor/ApiMonitor'),
    },
    {
      path: 'definition/edit/:definitionId',
      name: 'editCompleteContainer',
      component: () => import('@/business/components/api/definition/ApiDefinition'),
    },
    {
      path: 'messagesettings',
      name: 'MessageSettings',
      component: () => import('@/business/components/project/notification/MessageSettings'),
    },
  ]
};

import Layout from "metersphere-frontend/src/business/app-layout";

export default {
  path: "/api",
  name: "api",
  redirect: "/api/home",
  component: Layout,
  children: [
    {
      path: 'home',
      name: 'fucHome',
      component: () => import('@/business/home/ApiHome'),
    },
    {
      path: "automation/report",
      name: "ApiReportList",
      component: () => import('@/business/automation/report/ApiReportList'),
    },
    {
      path: "automation/report/:redirectID?/:dataType?/:dataSelectRange",
      name: "ApiReportListWithQuery",
      component: () => import('@/business/automation/report/ApiReportList'),
    },
    {
      path: "automation/report/view/:reportId",
      name: "ApiScenarioReportView",
      component: () => import('@/business/automation/report/ApiReportView'),
    },
    {
      path: "definition/report/view/:reportId",
      name: "ApiReportView",
      component: () => import('@/business/definition/components/response/ApiResponseView'),
    },
    {
      path: "definition",
      name: "ApiDefinition",
      component: () => import('@/business/definition/ApiDefinition'),
    },
    {
      path: "definition/:redirectID?/:dataType?/:dataSelectRange?/:projectId?/:type?/:workspaceId?",
      name: "ApiDefinitionWithQuery",
      component: () => import('@/business/definition/ApiDefinition'),
    },
    {
      path: "automation/:redirectID?/:dataType?/:dataSelectRange?/:projectId?/:workspaceId?",
      name: "ApiAutomationWithQuery",
      component: () => import('@/business/automation/ApiAutomation'),
    },
    {
      path: "automation",
      name: "ApiAutomation",
      component: () => import('@/business/automation/ApiAutomation'),
    },
    {
      path: 'definition/edit/:definitionId',
      name: 'editCompleteContainer',
      component: () => import('@/business/definition/ApiDefinition'),
    },
  ]
};


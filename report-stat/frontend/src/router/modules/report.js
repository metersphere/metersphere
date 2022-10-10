import Layout from "metersphere-frontend/src/business/app-layout";

export default {
  path: "/report",
  name: "report",
  redirect: "/report/project-statistics",
  component: Layout,
  children: [
    {
      path: '/report/project-statistics',
      name: 'projectStatistics',
      component: () => import('@/business/projectstatistics/ProjectStatistics'),
    },
    {
      path: "/report/project-report",
      name: "projectReport",
      component: () => import('@/business/enterprisereport/ProjectReport'),
    },
  ]
};


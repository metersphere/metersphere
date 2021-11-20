export default {
  path: "/report",
  name: "report",
  redirect: "/report/projectStatistics",
  components: {
    content: () => import('@/business/components/reportstatistics/ReportStatistics'),
  },
  children: [
    {
      path: '/report/projectStatistics',
      name: 'projectStatistics',
      component: () => import('@/business/components/reportstatistics/projectstatistics/ProjectStatistics'),
    },
    {
      path: "/report/projectReport",
      name: "projectReport",
      component: () => import('@/business/components/xpack/reportstatistics/projectreport/ProjectReport'),
    },
  ]
}

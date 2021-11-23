const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/);

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
    ...requireContext.keys().map(key => requireContext(key).enterpriseReport),
  ]
}

const reportForm = () => import('./ReportAnalysis');

export default {
  path: "/report",
  name: "report",
  redirect: "/report/home",
  components: {
    content: reportForm
  },
  children: [
    {
      path: 'home',
      name: 'reportHome',
    },
  ]
}

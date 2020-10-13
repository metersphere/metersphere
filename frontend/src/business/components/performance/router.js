import MsProject from "@/business/components/project/MsProject";

const PerformanceTest = () => import('@/business/components/performance/PerformanceTest')
const PerformanceTestHome = () => import('@/business/components/performance/home/PerformanceTestHome')
const EditPerformanceTestPlan = () => import('@/business/components/performance/test/EditPerformanceTestPlan')
const PerformanceTestPlan = () => import('@/business/components/performance/test/PerformanceTestPlan')
const PerformanceTestReport = () => import('@/business/components/performance/report/PerformanceTestReport')
const PerformanceChart = () => import('@/business/components/performance/report/components/PerformanceChart')
const PerformanceReportView = () => import('@/business/components/performance/report/PerformanceReportView')

export default {
  path: "/performance",
  name: "performance",
  redirect: "/performance/home",
  components: {
    content: PerformanceTest
  },
  children: [
    {
      path: 'home',
      name: 'perHome',
      component: PerformanceTestHome,
    },
    {
      path: 'test/create',
      name: "createPerTest",
      component: EditPerformanceTestPlan,
    },
    {
      path: "test/edit/:testId",
      name: "editPerTest",
      component: EditPerformanceTestPlan,
      props: {
        content: (route) => {
          return {
            ...route.params
          }
        }
      }
    },
    {
      path: "test/:projectId",
      name: "perPlan",
      component: PerformanceTestPlan
    },
    {
      path: "project/:type",
      name: "perProject",
      component: MsProject
    },
    {
      path: "report/:type",
      name: "perReport",
      component: PerformanceTestReport
    },
    {
      path: "chart",
      name: "perChart",
      component: PerformanceChart
    },
    {
      path: "report/view/:reportId",
      name: "perReportView",
      component: PerformanceReportView
    }
  ]
}

import Layout from "metersphere-frontend/src/business/app-layout";

const PerformanceTestHome = () => import('@/business/home/PerformanceTestHome')
const EditPerformanceTest = () => import('@/business/test/EditPerformanceTest')
const PerformanceTestList = () => import('@/business/test/PerformanceTestList')
const PerformanceTestReportList = () => import('@/business/report/PerformanceTestReportList')
const PerformanceChart = () => import('@/business/report/components/PerformanceChart')
const PerformanceReportView = () => import('@/business/report/PerformanceReportView')
const PerformanceReportCompare = () => import('@/business/report/PerformanceReportCompare')

export default {
  path: "/performance",
  name: "Performance",
  redirect: '/performance/home',
  component: Layout,
  children: [
    {
      path: 'home',
      name: 'performanceHome',
      component: PerformanceTestHome,
    },
    {
      path: 'test/create',
      name: "createPerTest",
      component: EditPerformanceTest,
    },
    {
      path: "test/edit/:testId",
      name: "editPerTest",
      component: EditPerformanceTest,
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
      component: PerformanceTestList
    },
    {
      path: "report/:type",
      name: "perReport",
      component: PerformanceTestReportList
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
    },
    {
      path: "report/compare/:reportId",
      name: "ReportCompare",
      component: PerformanceReportCompare,
    }
  ]
};


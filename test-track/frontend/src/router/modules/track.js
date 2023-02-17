import Layout from "metersphere-frontend/src/business/app-layout";

export default {
  path: "/track",
  name: "track",
  redirect: "/track/home",
  component: Layout,
  children: [
    {
      path: 'home',
      name: 'trackHome',
      component: () => import('@/business/home/TrackHome'),
    },
    {
      path: 'case/create',
      name: 'testCaseCreate',
      component: () => import('@/business/case/components/TestCaseEdit'),
    },
    {
      path: 'case/:projectId',
      name: 'testCase',
      component: () => import('@/business/case/TestCase'),
    },
    {
      path: 'case/all/:redirectID?/:dataType?/:dataSelectRange?/:projectId?/:workspaceId?',
      name: 'testCaseRedirect',
      component: () => import('@/business/case/TestCase'),
    },
    {
      path: 'case/edit/:caseId',
      name: 'testCaseEdit',
      component: () => import('@/business/case/components/TestCaseEdit'),
    },
    {
      path: 'testPlan/reportList',
      name: 'testPlanReportList',
      component: () => import('@/business/report/TestPlanReport'),
    },
    {
      path: 'issue/:id?/:projectId?/:dataSelectRange?',
      name: 'issueManagement',
      component: () => import('@/business/issue/IssueList.vue'),
    },
    {
      path: "plan/:type",
      name: "testPlan",
      component: () => import('@/business/plan/TestPlan')
    },
    {
      path: "plan/view/:planId",
      name: "planView",
      component: () => import('@/business/plan/view/TestPlanView')
    },
    {
      path: "plan/view/edit/:caseId",
      name: "planViewEdit",
      component: () => import('@/business/plan/view/TestPlanView')
    },
    {
      path: "review/:type",
      name: "testCaseReview",
      component: () => import('@/business/review/TestCaseReview'),
    },
    {
      path: "review/view/:reviewId",
      name: "testCaseReviewView",
      component: () => import('@/business/review/view/TestCaseReviewView')
    },
  ]
};


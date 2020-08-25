import MsProject from "@/business/components/project/MsProject";

const TestTrack = () => import(/* webpackChunkName: "track" */ '@/business/components/track/TestTrack')
const TrackHome = () => import(/* webpackChunkName: "track" */ '@/business/components/track/home/TrackHome')
const TestCase = () => import(/* webpackChunkName: "track" */ '@/business/components/track/case/TestCase')
const TestPlan = () => import(/* webpackChunkName: "track" */ '@/business/components/track/plan/TestPlan')
const TestPlanView = () => import(/* webpackChunkName: "track" */ '@/business/components/track/plan/view/TestPlanView')

export default {
  path: "/track",
  name: "track",
  redirect: "/track/home",
  components: {
    content: TestTrack
  },
  children: [
    {
      path: 'home',
      name: 'trackHome',
      component: TrackHome,
    },
    {
      path: 'case/create',
      name: 'testCaseCreate',
      component: TestCase,
    },
    {
      path: 'case/:projectId',
      name: 'testCase',
      component: TestCase,
    },
    {
      path: 'case/edit/:caseId',
      name: 'testCaseEdit',
      component: TestCase,
    },
    {
      path: "plan/:type",
      name: "testPlan",
      component: TestPlan
    },
    {
      path: "plan/view/:planId",
      name: "planView",
      component: TestPlanView
    },
    {
      path: "plan/view/edit/:caseId",
      name: "planViewEdit",
      component: TestPlanView
    },
    {
      path: "project/:type",
      name: "trackProject",
      component: MsProject
    }
  ]
}

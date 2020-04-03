import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "../../settings/Setting";
import User from "../../settings/system/User";
import EditPerformanceTestPlan from "../../performance/test/EditPerformanceTestPlan";
import PerformanceTestPlan from "../../performance/test/PerformanceTestPlan";
import Organization from "../../settings/system/Organization";
import OrganizationMember from "../../settings/organization/OrganizationMember";
import Member from "../../settings/workspace/WorkspaceMember";
import TestResourcePool from "../../settings/system/TestResourcePool";
import MsProject from "../../project/MsProject";
import OrganizationWorkspace from "../../settings/organization/OrganizationWorkspace";
import PersonSetting from "../../settings/personal/PersonSetting";
import SystemWorkspace from "../../settings/system/SystemWorkspace";
import PerformanceChart from "../../performance/report/components/PerformanceChart";
import PerformanceTestReport from "../../performance/report/PerformanceTestReport";
import FunctionalTestReport from "../../functional/report/FunctionalTestReport";
import FunctionalTest from "../../functional/FunctionalTest";
import PerformanceTest from "../../performance/PerformanceTest";
import EditFunctionalTestPlan from "../../functional/test/EditFunctionalTestPlan";
import PerformanceTestHome from "../../performance/home/PerformanceTestHome";
import FunctionalTestPlan from "../../functional/test/FunctionalTestPlan";
import FunctionalTestHome from "../../functional/home/FunctionalTestHome";
import PerformanceReportView from "../../performance/report/PerformanceReportView";
import FunctionalReportView from "../../functional/report/FunctionalReportView";
import TrackHome from "../../track/home/TrackHome";
import TestPlan from "../../track/plan/TestPlan";
import TestPlanView from "../../track/plan/TestPlanView";
import TestCase from "../../track/case/TestCase";
import TestTrack from "../../track/TestTrack";

Vue.use(VueRouter);

const router = new VueRouter({
  routes: [
    {path: "/", redirect: '/setting/personsetting'},
    {
      path: "/sidebar",
      components: {
        sidebar: RouterSidebar
      }
    },
    {
      path: "/setting",
      components: {
        content: Setting
      },
      children: [
        {
          path: 'user',
          component: User,
        },
        {
          path: 'organization',
          component: Organization,
        },
        {
          path: 'organizationmember',
          component: OrganizationMember,
        },
        {
          path: 'organizationworkspace',
          component: OrganizationWorkspace,
        },
        {
          path: 'personsetting',
          component: PersonSetting
        },
        {
          path: 'member',
          component: Member
        },
        {
          path: 'systemworkspace',
          component: SystemWorkspace
        },
        {
          path: 'testresourcepool',
          component: TestResourcePool
        }
      ]
    },
    {
      path: "/functional",
      name: "functional",
      redirect: "/functional/home",
      components: {
        content: FunctionalTest
      },
      children: [
        {
          path: 'home',
          name: 'fucHome',
          component: FunctionalTestHome,
        },
        {
          path: 'test/create',
          name: "createFucTest",
          component: EditFunctionalTestPlan,
        },
        {
          path: "test/edit/:testId",
          name: "editFucTest",
          component: EditFunctionalTestPlan,
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
          name: "fucPlan",
          component: FunctionalTestPlan
        },
        {
          path: "project/:type",
          name: "fucProject",
          component: MsProject
        },
        {
          path: "report/:type",
          name: "fucReport",
          component: FunctionalTestReport
        },
        {
          path: "report/view/:reportId",
          name: "fucReportView",
          component: FunctionalReportView
        }
      ]
    },
    {
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
    },
    {
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
          path: 'case/:caseId',
          name: 'testCase',
          component: TestCase,
        },
        {
          path: "plan/:projectId",
          name: "testPlan",
          component: TestPlan
        },
        {
          path: "plan/view/:planId",
          name: "planView",
          component: TestPlanView
        },
        {
          path: "project/:type",
          name: "trackProject",
          component: MsProject
        }
      ]
    }
  ]
});

export default router

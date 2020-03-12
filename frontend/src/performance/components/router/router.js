import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "../settings/Setting";
import User from "../settings/system/User";
import EditPerformanceTestPlan from "../testPlan/EditPerformanceTestPlan";
import PerformanceTestPlan from "../testPlan/PerformanceTestPlan";
import Organization from "../settings/system/Organization";
import OrganizationMember from "../settings/organization/OrganizationMember";
import Member from "../settings/workspace/WorkspaceMember";
import TestResourcePool from "../settings/system/TestResourcePool";
import MsProject from "../project/MsProject";
import OrganizationWorkspace from "../settings/organization/OrganizationWorkspace";
import PersonSetting from "../settings/personal/PersonSetting";
import SystemWorkspace from "../settings/system/SystemWorkspace";
import PerformanceChart from "../project/PerformanceChart";
import PerformanceTestReport from "../report/PerformanceTestReport";
import FunctionalTestReport from "../report/FunctionalTestReport";
import FunctionalTest from "../testPlan/FunctionalTest";
import PerformanceTest from "../testPlan/PerformanceTest";
import EditFunctionalTestPlan from "../testPlan/EditFunctionalTestPlan";
import PerformanceTestHome from "../testPlan/PerformanceTestHome";
import FunctionalTestPlan from "../testPlan/FunctionalTestPlan";
import FunctionalTestHome from "../testPlan/FunctionalTestHome";

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
          component: FunctionalTestHome,
        },
        {
          path: 'plan/create',
          name: "createFucTest",
          component: EditFunctionalTestPlan,
        },
        {
          path: "plan/edit/:testId",
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
          path: "plan/:projectId",
          component: FunctionalTestPlan
        },
        {
          path: "project/:type",
          component: MsProject
        },
        {
          path: "report/:type",
          component: FunctionalTestReport
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
          component: PerformanceTestHome,
        },
        {
          path: 'plan/create',
          name: "createPerTest",
          component: EditPerformanceTestPlan,
        },
        {
          path: "plan/edit/:testId",
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
          path: "plan/:projectId",
          component: PerformanceTestPlan
        },
        {
          path: "project/:type",
          component: MsProject
        },
        {
          path: "report/:type",
          component: PerformanceTestReport
        },
        {
          path: "chart",
          component: PerformanceChart
        }
      ]
    }
  ]
});

export default router

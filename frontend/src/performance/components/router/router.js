import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "../settings/Setting";
import User from "../settings/system/User";
import EditTestPlan from "../testPlan/EditTestPlan";
import AllTestPlan from "../testPlan/AllTestPlan";
import Organization from "../settings/system/Organization";
import OrganizationMember from "../settings/organization/OrganizationMember";
import Member from "../settings/workspace/WorkspaceMember";
import TestResourcePool from "../settings/system/TestResourcePool";
import MsProject from "../project/MsProject";
import OrganizationWorkspace from "../settings/organization/OrganizationWorkspace";
import PersonSetting from "../settings/personal/PersonSetting";
import SystemWorkspace from "../settings/system/SystemWorkspace";

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
      path: "/createTest",
      name: "createTest",
      components: {
        content: EditTestPlan
      }
    },
    {
      path: "/editTest/:testId",
      name: "editTest",
      components: {
        content: EditTestPlan
      },
      props: {
        content: (route) => {
          return {
            ...route.params
          }
        }
      }
    },
    {
      path: "/loadtest/:projectId",
      name: "loadtest",
      components: {
        content: AllTestPlan
      },
    },
    {
      path: "/project/:type",
      name: 'project',
      components: {
        content: MsProject
      }
    }
  ]
});

export default router

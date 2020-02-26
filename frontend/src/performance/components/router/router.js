import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "../settings/Setting";
import Workspace from "../settings/Workspace";
import User from "../settings/User";
import CreateTestPlan from "../testPlan/CreateTestPlan";
import AllTestPlan from "../testPlan/AllTestPlan";
import Organization from "../settings/Organization";
import OrganizationMember from "../settings/OrganizationMember";
import Member from "../settings/Member";
import TestResourcePool from "../settings/TestResourcePool";
import MsProject from "../project/MsProject";
import OrganizationWorkspace from "../settings/OrganizationWorkspace";
import PersonSetting from "../settings/PersonSetting";
import SystemWorkspace from "../settings/SystemWorkspace";

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
          path: 'workspace',
          component: Workspace,
        },
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
        content: CreateTestPlan
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
      path: "/allTest/:projectId",
      name: "allTest",
      components: {
        content: AllTestPlan
      },
    },
    {
      path: "/project",
      name: 'project',
      components: {
        content: MsProject
      }
    }
  ]
});

export default router

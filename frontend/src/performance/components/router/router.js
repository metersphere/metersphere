import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "../settings/Setting";
import Workspace from "../settings/Workspace";
import User from "../settings/User";
import CreateTestPlan from "../testPlan/CreateTestPlan";
import AllTestPlan from "../testPlan/AllTestPlan";
import Organization from "../settings/Organization";
import Member from "../settings/Member";
import TestResourcePool from "../settings/TestResourcePool";
import MsProject from "../project/MsProject";

Vue.use(VueRouter);

const router = new VueRouter({
  routes: [
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
          meta: {
            roles: ['org_admin']
          }
        },
        {
          path: 'user',
          component: User,
          meta: {
            roles: ['admin']
          }
        },
        {
          path: 'organization',
          component: Organization,
          meta: {
            roles: ['admin']
          }
        },
        {
          path: 'member',
          component: Member
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
      path: "/allTest",
      components: {
        content: AllTestPlan
      }
    },
    {
      path: "/project",
      components: {
        content: MsProject
      }
    }
  ]
});

export default router

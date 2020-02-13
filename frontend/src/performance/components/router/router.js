import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "../settings/Setting";
import Workspace from "../settings/Workspace";
import User from "../settings/User";
import CreateTestPlan from "../testPlan/CreateTestPlan";
import Organization from "../settings/Organization";
import WorkspaceUser from "../settings/WorkSpcaeUser";
import TestResourcePool from "../settings/TestResourcePool";

Vue.use(VueRouter);

const router = new VueRouter({
  routes: [
    {
      path: "/sidebar", components: {
        sidebar: RouterSidebar
      }
    },
    {
      path: "/content", components: {
        content: Setting
      },
      children: [
        {
          path: 'workspace',
          component: Workspace,
          meta: {
            roles: ['admin']
          }
        },
        {
          path: 'user',
          component: User
        },
        {
          path: 'organization',
          component: Organization
        },{
          path:'workspace/user',
          component: WorkspaceUser
        },
        {
          path: 'testresourcepool',
          component: TestResourcePool
        }
      ]
    },
    {
      path: "/createTest", components: {
        content: CreateTestPlan
      }
    },
  ]
});

export default router

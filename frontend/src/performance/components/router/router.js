import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "../settings/Setting";
import Workspace from "../settings/Workspace";
import User from "../settings/User";
import CreateTestPlan from "../testPlan/CreateTestPlan";
import Organization from "../settings/Organization";
import WorkspaceUser from "../settings/WorkSpcaeUser";

Vue.use(VueRouter);

const router = new VueRouter({
  routes: [
    {
      path: "/sidebar", components: {
        sidebar: RouterSidebar
      }
    },
    {
      path: "/setting", components: {
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
          path: 'workspace/user',
          component: WorkspaceUser
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

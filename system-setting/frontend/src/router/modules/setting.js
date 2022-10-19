import Layout from "metersphere-frontend/src/business/app-layout";

const Setting = {
  path: "/setting",
  name: "Setting",
  component: Layout,
  redirect: "/setting/dashboard",
  children: [
    {
      path: 'dashboard',
      name: "SettingHome",
      component: () => import('../../business/SettingHome'),
      meta: {}
    },
    {
      path: 'user',
      component: () => import('../../business/system/user/User'),
      meta: {system: true, title: 'commons.user', permissions: ['SYSTEM_USER:READ']}
    },
    {
      path: 'systemworkspace',
      component: () => import('../../business/system/SystemWorkspace'),
      meta: {system: true, title: 'commons.workspace', permissions: ['SYSTEM_WORKSPACE:READ']}
    },
    {
      path: 'usergroup',
      component: () => import('../../business/system/group/UserGroup'),
      meta: {
        system: true,
        title: 'group.group_permission',
        permissions: ['SYSTEM_GROUP:READ']
      }
    },
    {
      path: 'testresourcepool',
      component: () => import('../../business/system/pool/TestResourcePool'),
      meta: {system: true, title: 'commons.test_resource_pool', permissions: ['SYSTEM_TEST_POOL:READ']}
    },
    {
      path: 'systemparametersetting',
      component: () => import('../../business/system/setting/SystemParameterSetting'),
      meta: {system: true, title: 'commons.system_parameter_setting', permissions: ['SYSTEM_SETTING:READ']}
    },
    {
      path: 'member',
      component: () => import('../../business/workspace/member/WorkspaceMember'),
      meta: {workspace: true, title: 'commons.member', permissions: ['WORKSPACE_USER:READ']}
    },
    {
      path: 'serviceintegration',
      component: () => import('../../business/workspace/integration/ServiceIntegration'),
      meta: {workspace: true, title: 'organization.service_integration', permissions: ['WORKSPACE_SERVICE:READ']}
    },
    {
      path: 'personsetting',
      name: 'PersonSetting',
      component: () => import('metersphere-frontend/src/components/personal/PersonSetting'),
      meta: {person: true, title: 'commons.personal_setting'}
    },
    {
      path: 'apikeys',
      component: () => import('metersphere-frontend/src/components/personal/ApiKeys'),
      meta: {
        person: true,
        title: 'commons.api_keys',
        roles: ['test_manager', 'test_user', 'test_viewer', 'org_admin', 'admin']
      }
    },
    {
      path: 'project/quota',
      component: () => import('../../business/workspace/quota/MxProjectQuota'),
      meta: {
        workspace: true, valid: true, xpack: true, title: 'commons.quota', permissions: ['WORKSPACE_QUOTA:READ'],
      },
    },
    {
      path: 'workspace/quota',
      component: () => import('../../business/system/quota/MxWorkspaceQuota'),
      meta: {system: true, valid: true, xpack: true, title: 'commons.quota', permissions: ['SYSTEM_QUOTA:READ']}
    },
    {
      path: 'license',
      component: () => import('../../business/system/license/MxLicense'),
      meta: {system: true, valid: true, title: 'license.title', permissions: ['SYSTEM_AUTH:READ']}
    },
    {
      path: 'project/:type',
      component: () => import('../../business/workspace/project/MsProject'),
      meta: {workspace: true, title: 'project.manager', permissions: ['WORKSPACE_PROJECT_MANAGER:READ']}
    },
    {
      path: 'wsenvlist',
      component: () => import('../../business/workspace/environment/EnvironmentManage'),
      meta: {
        workspace: true,
        title: 'api_test.environment.environment_manage',
        permissions: ['WORKSPACE_PROJECT_ENVIRONMENT:READ']
      }
    },
    {
      path: 'operatingLog/system',
      component: () => import('../../business/system/log/OperatingLog'),
      name: 'system',
      meta: {system: true, title: 'operating_log.title', permissions: ['SYSTEM_OPERATING_LOG:READ']}
    },
    {
      path: 'operatingLog/workspace',
      component: () => import('../../business/workspace/log/OperatingLog'),
      name: 'workspace',
      meta: {workspace: true, title: 'operating_log.title', permissions: ['WORKSPACE_OPERATING_LOG:READ']}
    },
    {
      path: 'plugin',
      component: () => import('../../business/system/plugin/PluginConfig'),
      meta: {system: true, title: 'plugin.title', permissions: ['SYSTEM_PLUGIN:READ']}
    },
  ]
};

export default Setting;

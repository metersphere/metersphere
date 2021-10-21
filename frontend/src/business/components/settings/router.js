const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/);

export default {
  path: "/setting",
  name: "Setting",
  components: {
    content: () => import('@/business/components/settings/Setting')
  },
  children: [
    {
      path: '',
      name: "SettingHome",
      component: () => import('@/business/components/settings/SettingHome'),
      meta: {}
    },
    {
      path: 'user',
      component: () => import('@/business/components/settings/system/User'),
      meta: {system: true, title: 'commons.user', permissions: ['SYSTEM_USER:READ']}
    },
    {
      path: 'systemworkspace',
      component: () => import('@/business/components/settings/system/SystemWorkspace'),
      meta: {system: true, title: 'commons.workspace', permissions: ['SYSTEM_WORKSPACE:READ']}
    },
    {
      path: 'usergroup',
      component: () => import('@/business/components/settings/system/group/UserGroup'),
      meta: {system: true, title: 'group.group_permission', permissions: ['SYSTEM_GROUP:READ', 'ORGANIZATION_GROUP:READ']}
    },
    {
      path: 'workspace/template/field',
      component: () => import('@/business/components/settings/workspace/template/CustomFieldList'),
      meta: {workspaceTemplate: true, title: 'custom_field.name', permissions: ['WORKSPACE_TEMPLATE:READ+CUSTOM']},
    },
    {
      path: 'workspace/template/case',
      component: () => import('@/business/components/settings/workspace/template/TestCaseTemplateList'),
      meta: {workspaceTemplate: true, title: 'workspace.case_template_manage', permissions: ['WORKSPACE_TEMPLATE:READ+CASE_TEMPLATE']},
    },
    {
      path: 'workspace/template/issues',
      component: () => import('@/business/components/settings/workspace/template/IssuesTemplateList'),
      meta: {workspaceTemplate: true, title: 'workspace.issue_template_manage', permissions: ['WORKSPACE_TEMPLATE:READ+ISSUE_TEMPLATE']},
    },
    {
      path: 'testresourcepool',
      component: () => import('@/business/components/settings/system/TestResourcePool'),
      meta: {system: true, title: 'commons.test_resource_pool', permissions: ['SYSTEM_TEST_POOL:READ']}
    },
    {
      path: 'systemparametersetting',
      component: () => import('@/business/components/settings/system/SystemParameterSetting'),
      meta: {system: true, title: 'commons.system_parameter_setting', permissions: ['SYSTEM_SETTING:READ']}
    },
    ...requireContext.keys().map(key => requireContext(key).system),
    ...requireContext.keys().map(key => requireContext(key).license),
    {
      path: 'member',
      component: () => import('@/business/components/settings/workspace/WorkspaceMember'),
      meta: {workspace: true, title: 'commons.member', permissions: ['WORKSPACE_USER:READ']}
    },
    {
      path: 'serviceintegration',
      component: () => import('@/business/components/settings/workspace/ServiceIntegration'),
      meta: {workspace: true, title: 'organization.service_integration', permissions: ['WORKSPACE_SERVICE:READ']}
    },
    {
      path: 'messagesettings',
      component: () => import('@/business/components/settings/workspace/MessageSettings'),
      meta: {workspace: true, title: 'organization.message_settings', permissions: ['WORKSPACE_MESSAGE:READ']}
    },
    {
      path: 'personsetting',
      name: 'PersonSetting',
      component: () => import('@/business/components/settings/personal/PersonSetting'),
      meta: {person: true, title: 'commons.personal_setting'}
    },
    {
      path: 'apikeys',
      component: () => import('@/business/components/settings/personal/ApiKeys'),
      meta: {
        person: true,
        title: 'commons.api_keys',
        roles: ['test_manager', 'test_user', 'test_viewer', 'org_admin', 'admin']
      }
    },
    {
      path: 'project/:type',
      component: () => import('@/business/components/settings/workspace/MsProject'),
      meta: {workspace: true, title: 'project.manager', permissions: ['WORKSPACE_PROJECT_MANAGER:READ']}
    },
    {
      path: 'wsenvlist',
      component: () => import('@/business/components/settings/workspace/WsEnvironmentList'),
      meta: {workspace: true, title: 'api_test.environment.environment_config', permissions: ['WORKSPACE_PROJECT_ENVIRONMENT:READ']}
    },
    {
      path: 'operatingLog/system',
      component: () => import('@/business/components/settings/operatinglog/OperatingLog'),
      name: 'system',
      meta: {system: true, title: 'operating_log.title', permissions: ['SYSTEM_OPERATING_LOG:READ']}
    },
    {
      path: 'operatingLog/workspace',
      component: () => import('@/business/components/settings/operatinglog/OperatingLog'),
      name: 'workspace',
      meta: {workspace: true, title: 'operating_log.title', permissions: ['WORKSPACE_OPERATING_LOG:READ']}
    },
    {
      path: 'plugin',
      component: () => import('@/business/components/settings/plugin/PluginConfig'),
      meta: {system: true, title: 'plugin.title', permissions: ['SYSTEM_USER:READ']}
    },
  ]
};

import el from "metersphere-frontend/src/i18n/lang/ele-en-US";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  user: {
    search_get_more_tip: 'Search for other options',
    remove_group_tip: 'Have at least one user group permission'
  },
  system: {
    environment_import_repeat_tip: "(Environment configuration with the same name filtered {0})",
    search_by_environment_name: "search by environment name",
    check_third_project_success: "inspection passed",
    api_default_run_message: 'In order not to affect the normal execution of the interface, please configure the resource pool for interface execution in [Project Settings - Application Management - Interface Test]',
    api_default_run: 'The interface is executed locally by default',
    test_resource_pool: {
      edit_job_template: "Edit Job Template",
      edit_job_template_tip: "The Kubernetes Job template is a text in YAML format that defines the running parameters of the Job. You can edit the Job template here.",
    },
    operating_log: {
      plugin_manage: "Plugin Manage",
    }
  },
  display: {
    title: 'Theme',
    logo: 'System LOGO',
    loginLogo: 'Login page upper left corner LOGO',
    loginImage: 'Picture on the right side of the login page',
    loginTitle: 'Login page prompt information',
    pageTitle: 'Page Title',
    sysTitle: 'System Name',
    theme_style: 'Theme style set',
    theme_default: 'Default',
    theme_light: 'White',
    theme_follow: 'Follow the theme color',
    css_file: 'Custom CSS',
  },
  system_custom_template: {
    api_template: "Api Template",
  },
  qrcode:{
    service_unconfigured: 'Not configured',
    service_configured: 'configured',
    service_unconfiguredTip: 'Not configured yet, can be configured and enabled on the edit page',
    service_resetConfigTip: 'Reset successful!',
    service_testLink: 'test connection',
    service_testLinkStatusTip: 'Test connection successful!',
    service_enableSuccess: 'Activated successfully',
    service_closeSuccess: 'Disabled successfully',
    service_edit: 'edit',
    service_reset: 'reset',
    service_WE_COM: 'WE_COM',
    service_DING_TALK: 'DING_TALK',
    service_LARK: 'LARK',
    service_LARK_SUITE: 'LARK_SUITE',
    service_corpId: 'Corp Id',
    service_agentId: 'Agent Id',
    service_appKey: 'App Key',
    service_appSecret: 'App Secret',
    service_callBack: 'CallBack',
    service_enable: 'state',
    service_valid: 'Is it available',
    service_corpId_required: 'Corp ID cannot be empty',
    service_agentId_required: 'Agent Id cannot be empty',
    service_appKey_required: 'App Key cannot be empty',
    service_appSecret_required: 'App Secret cannot be empty',
    service_callBack_required: 'CallBack cannot be empty',
  }
}
export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


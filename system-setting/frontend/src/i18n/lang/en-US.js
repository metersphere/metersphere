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
    theme_follow: 'Follow the theme color'
  },
  system_custom_template: {
    api_template: "Api Template",
  }
}
export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


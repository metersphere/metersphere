import el from "metersphere-frontend/src/i18n/lang/ele-zh-CN"; // 加载element的内容
import fu from "fit2cloud-ui/src/locale/lang/zh-CN"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-CN"

const message = {
  system_user: {
    search_get_more_tip: '搜索获取其他选项',
    remove_group_tip: '至少拥有一个用户组权限'
  },
  system: {
    environment_import_repeat_tip: "(已过滤同名称的环境配置 {0})",
    search_by_environment_name: "根据环境的名称搜索",
    check_third_project_success: "检查通过",
    api_default_run_message: '为了不影响接口正常执行，请在【 项目设置-应用管理-接口测试 】中配置接口执行的资源池',
    api_default_run: '接口默认本地执行',
    test_resource_pool: {
      edit_job_template: "编辑Job模版",
      edit_job_template_tip: "Kubernetes Job模版是一个YAML格式的文本，用于定义Job的运行参数，您可以在此处编辑Job模版。",
    },
    operating_log: {
      plugin_manage: "插件管理",
    }
  },
  display: {
    title: '显示设置',
    logo: '系统 LOGO',
    loginLogo: '登录页左上角 LOGO',
    loginImage: '登录页面右侧图片',
    loginTitle: '登录页面提示信息',
    pageTitle: '页面 Title',
    sysTitle: '系统名称',
    theme_style: '主题风格设置',
    theme_default: '默认',
    theme_light: '白色',
    theme_follow: '跟随主题色'
  },
  system_custom_template: {
    api_template: "接口模版"
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message,
};

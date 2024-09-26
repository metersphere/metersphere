import el from "metersphere-frontend/src/i18n/lang/ele-zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW"

const message = {
  user: {
    search_get_more_tip: '搜索獲取其他選項',
    remove_group_tip: '至少擁有一個用戶組權限'
  },
  system: {
    environment_import_repeat_tip: "(已過濾同名稱的環境配置 {0})",
    search_by_environment_name: "根據環境的名稱搜索",
    check_third_project_success: "檢查通過",
    api_default_run_message: '為了不影響接口正常執行，請在【 項目設置-應用管理-接口測試 】中配置接口執行的資源池',
    api_default_run: '接口默認本地執行',
    test_resource_pool: {
      edit_job_template: "編輯Job模版",
      edit_job_template_tip: "Kubernetes Job模版是一個YAML格式的文本，用於定義Job的運行參數，您可以在此處編輯Job模版。",
    },
    operating_log: {
      plugin_manage: "插件管理",
    }
  },
  display: {
    title: '顯示設置',
    logo: '系統 LOGO',
    loginLogo: '登錄頁左上角 LOGO',
    loginImage: '登錄頁面右側圖片',
    loginTitle: '登錄頁面提示信息',
    pageTitle: '頁面 Title',
    sysTitle: '系統名稱',
    theme_style: '主題風格設置',
    theme_default: '默認',
    theme_light: '白色',
    theme_follow: '跟隨主題色',
    css_file: '自定義CSS',
  },
  system_custom_template: {
    api_template: "接口模版",
  },
  qrcode:{
    service_unconfigured: '未配置',
    service_configured: '已配置',
    service_unconfiguredTip: '暫未配置，可在編輯頁配置並開啟',
    service_resetConfigTip: '重置成功！',
    service_testLink: '測試連接',
    service_testLinkStatusTip: '測試連接成功！',
    service_testLinkStatusErrorTip: '測試連接失敗！',
    service_enableSuccess: '啟用成功',
    service_closeSuccess: '停用成功',
    service_edit: '編輯',
    service_reset: '重置',
    service_WE_COM: '企業微信',
    service_DING_TALK: '釘釘',
    service_LARK: '飛書',
    service_LARK_SUITE: '國際飛書',
    service_corpId: '企業ID',
    service_agentId: '應用ID',
    service_appKey: '應用key',
    service_appSecret: '應用程式密鑰',
    service_callBack: '回調域名',
    service_enable: '狀態',
    service_valid: '是否可用',
    service_corpId_required: '企業ID不能為空',
    service_agentId_required: '應用ID不能為空',
    service_appKey_required: '應用key不能為空',
    service_appSecret_required: '應用程式密鑰不能為空',
    service_callBack_required: '回調域名不能為空',
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

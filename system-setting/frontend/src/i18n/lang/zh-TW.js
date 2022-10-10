import el from "element-ui/lib/locale/lang/zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW"

const message = {
  user: {
    search_get_more_tip: '搜索獲取其他選項'
  },
  display: {
    title: '顯示設置',
    logo: '系統 LOGO',
    loginLogo: '登錄頁左上角 LOGO',
    loginImage: '登陸頁面右側圖片',
    loginTitle: '登陸頁面提示信息',
    pageTitle: '頁面 Title',
    sysTitle: '系統名稱',
    theme_style: '主題風格設置',
    theme_default: '默認',
    theme_light: '白色',
    theme_follow: '跟隨主題色'
  },
  system_custom_template: {
    api_template: "接口模版",
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

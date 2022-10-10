import el from "element-ui/lib/locale/lang/en";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  user: {
    search_get_more_tip: 'Search for other options'
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


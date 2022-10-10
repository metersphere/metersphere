import el from "element-ui/lib/locale/lang/zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW"

const message = {
  api_case: {
    please_add_api_case: "请先添加接口用例",
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

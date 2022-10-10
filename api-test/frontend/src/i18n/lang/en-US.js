import el from "element-ui/lib/locale/lang/en";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  api_case: {
    please_add_api_case: "Please add api case",
  }
}
export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


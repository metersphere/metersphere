import el from "element-ui/lib/locale/lang/zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW"

const message = {
  performance_test: {
    report: {
      test_duration: '{0}時{1}分{2}秒',
      test_duration_tips: '執行時長：{0}時{1}分{2}秒',
    }
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

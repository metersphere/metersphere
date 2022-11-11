import el from "element-ui/lib/locale/lang/zh-CN"; // 加载element的内容
import fu from "fit2cloud-ui/src/locale/lang/zh-CN"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-CN"

const message = {
  performance_test: {
    report: {
      test_duration: '{0}时{1}分{2}秒',
      test_duration_tips: '执行时长：{0}时{1}分{2}秒',
    }
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

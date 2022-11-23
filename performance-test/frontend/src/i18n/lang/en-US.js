import el from "element-ui/lib/locale/lang/en";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  performance_test: {
    report: {
      test_duration: '{0} hours {1} minutes {2} seconds',
      test_duration_tips: 'Execution Time：{0} hours {1} minutes {2} seconds',
    },
    sync_scenario_no_permission_tips: 'No permission to create the scenario cannot perform synchronization',
  }
}
export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


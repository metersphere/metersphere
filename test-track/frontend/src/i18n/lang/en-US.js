import el from "element-ui/lib/locale/lang/en";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  home: {
    table: {
      index: "Index",
      task_type: "Task Type",
      run_rule: "Rule",
      task_status: "Status",
      next_execution_time: "Next Execution Time",
      create_user: "Creator",
      update_time: "Update time",
    },
    case: {
      index: "Ranking",
      case_name: "Case Name",
      case_type: "Case Type",
      test_plan: "Test Plan",
      failure_times: "Failure times",
    }
  },
  plan: {
    batch_delete_tip: "Do you want to continue deleting the test plan?",
  }
}
export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


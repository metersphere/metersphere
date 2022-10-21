import el from "element-ui/lib/locale/lang/en";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  api_case: {
    please_add_api_case: "Please add api case",
  },
  api_definition: {
    document: {
      name: "name",
      value: "value",
      is_required: "Is it required",
      desc: "Description",
      type: "Type",
      default_value: "Default value"
    },
    body: {
      json_format_error: "JSON format error"
    }
  },
  home:{
    table: {
      index: "Index",
      scenario: "Scenario",
      task_type: "Task Type",
      run_rule: "Rule",
      task_status: "Status",
      next_execution_time: "Next Execution Time",
      create_user: "Creator",
      update_time: "Update time",
      case_pass: "Case pass rate",
      scenario_pass: "Scenario pass rate",
    },
    case: {
      index: "Ranking",
      case_name: "Case Name",
      case_type: "Case Type",
      test_plan: "Test Plan",
      failure_times: "Failure times",
    },
    new_case: {
      index: "ID",
      api_name: "Api Name",
      path: "path",
      api_status: "Api status",
      update_time: "Update time",
      relation_case: "Relation CASE",
      relation_scenario: "Relation Scenario"
    },
  }
}
export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


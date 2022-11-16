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
    },
    rate: {
      case_review: "Review rate",
      case_review_pass: "Review pass rate",
      cover: "Cover rate",
      legacy: "Legacy rate",
      legacy_issue: "Percentage of legacy defects",
    },
    dashboard: {
      public: {
        this_week: "This week ",
        load_error: "Loading failure",
        no_data: "No data",
      },
      case_finished_review_pass_tip: "Reviewed cases/All reviewed cases *100%"
    },
    case_review_dashboard: {
      case_count: "Case count",
      not_review: "Not reviewed",
      finished_review: "Reviewed",
      not_pass: "Not pass",
      pass: "Pass",
    },
    relevance_dashboard: {
      api_case: "Api case",
      scenario_case: "Scenario case",
      performance_case: "Performance case",
      relevance_case_count: "Relevance case count",
      not_cover: "Not cover",
      cover: "Cover",
    },
    bug_dashboard: {
      un_closed_bug_count: "Unclosed bug count",
      un_closed_range: "Unclosed bug range",
      un_closed_range_tips: "Unclosed bugs/all associated bugs *100%",
      un_closed_bug_case_range: "Unclosed bug case range",
      un_closed_bug_case_range_tips: "Unclosed bugs/all associated cases *100%",
      un_closed_count: "Unclosed bug count",
      total_count: "Bug total",
      case_count: "Case count",
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


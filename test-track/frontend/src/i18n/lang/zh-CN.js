import el from "element-ui/lib/locale/lang/zh-CN"; // 加载element的内容
import fu from "fit2cloud-ui/src/locale/lang/zh-CN"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-CN"

const message = {
  home: {
    table: {
      index: "序号",
      task_type: "任务类型",
      run_rule: "运行规则",
      task_status: "任务状态",
      next_execution_time: "下次执行时间",
      create_user: "创建人",
      update_time: "更新时间",
    },
    case: {
      index: "排名",
      case_name: "用例名称",
      case_type: "用例类型",
      test_plan: "所属测试计划",
      failure_times: "失败次数",
    },
    rate: {
      case_review: "评审率",
      case_review_pass: "评审通过率",
      cover: "覆盖率",
      legacy: "遗留率",
      legacy_issue: "遗留缺陷占比",
    },
    dashboard: {
      public: {
        this_week: "本周",
        load_error: "加载失败",
        no_data: "暂无数据",
      },
      case_finished_review_pass_tip: "已评审通过的案例/所有完成评审的案例*100%"
    },
    case_review_dashboard: {
      case_count: "用例数量",
      not_review: "未评审",
      finished_review: "已评审",
      not_pass: "未通过",
      pass: "已通过",
    },
    relevance_dashboard: {
      api_case: "接口用例",
      scenario_case: "场景用例",
      performance_case: "性能用例",
      relevance_case_count: "关联用例数量",
      not_cover: "未覆盖",
      cover: "已覆盖",
    },
    bug_dashboard: {
      un_closed_bug_count: "遗留缺陷",
      un_closed_range: "遗留率",
      un_closed_range_tips: "未关闭缺陷/所有关联的缺陷*100%",
      un_closed_bug_case_range: "遗留缺陷占比",
      un_closed_bug_case_range_tips: "未关闭缺陷/所有关联的用例*100%",
      un_closed_count: "遗留缺陷数",
      total_count: "缺陷总数",
      case_count: "用例总数",
    }
  },
  plan: {
    batch_delete_tip: "批量删除测试计划，是否继续?",
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

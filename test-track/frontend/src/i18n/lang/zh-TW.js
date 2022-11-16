import el from "element-ui/lib/locale/lang/zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW"

const message = {
  home: {
    table: {
      index: "序號",
      task_type: "任務類型",
      run_rule: "運行規則",
      task_status: "任務狀態",
      next_execution_time: "下次執行時間",
      create_user: "創建人",
      update_time: "更新時間",
    },
    case: {
      index: "排名",
      case_name: "用例名稱",
      case_type: "用例類型",
      test_plan: "所屬測試計劃",
      failure_times: "失敗次數",
    },
    rate: {
      case_review: "評審率",
      case_review_pass: "評審通過率",
      cover: "覆蓋率",
      legacy: "遺留率",
      legacy_issue: "遺留缺陷佔比",
    },
    dashboard: {
      public: {
        this_week: "本週",
        load_error: "加載失敗",
        no_data: "暫無數據",
      },
      case_finished_review_pass_tip: "已評審通過的案例/所有完成評審的案例*100%"
    },
    case_review_dashboard: {
      case_count: "用例數量",
      not_review: "未評審",
      finished_review: "已評審",
      not_pass: "未通過",
      pass: "已通過",
    },
    relevance_dashboard: {
      api_case: "接口用例",
      scenario_case: "場景用例",
      performance_case: "性能用例",
      relevance_case_count: "關聯用例數量",
      not_cover: "未覆蓋",
      cover: "已覆蓋",
    },
    bug_dashboard: {
      un_closed_bug_count: "遺留缺陷",
      un_closed_range: "遺留率",
      un_closed_range_tips: "未關閉缺陷/所有關聯的缺陷*100%",
      un_closed_bug_case_range: "遺留缺陷佔比",
      un_closed_bug_case_range_tips: "未關閉缺陷/所有關聯的用例*100%",
      un_closed_count: "遺留缺陷數",
      total_count: "缺陷總數",
      case_count: "用例總數",
    }
  },
  plan: {
    batch_delete_tip: "批量刪除測試計劃，是否繼續？",
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

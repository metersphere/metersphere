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
    }
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

import el from "element-ui/lib/locale/lang/zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW"

const message = {
  api_case: {
    please_add_api_case: "请先添加接口用例",
  },
  api_definition: {
    document: {
      name: "名稱",
      value: "值",
      is_required: "是否必填",
      desc: "描述",
      type: "類型",
      default_value: "默認值",
    },
    body: {
      json_format_error: "JSON格式錯誤",
    }
  },
  home: {
    table: {
      index: "序號",
      scenario: "場景名稱",
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
    new_case: {
      index: "ID",
      api_name: "接口名稱",
      path: "路徑",
      api_status: "狀態",
      update_time: "更新時間",
      relation_case: "關聯CASE",
      relation_scenario: "關聯場景"
    },
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

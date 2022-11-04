import el from "element-ui/lib/locale/lang/zh-CN"; // 加载element的内容
import fu from "fit2cloud-ui/src/locale/lang/zh-CN"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-CN"

const message = {
  api_case: {
    please_add_api_case: "请先添加接口用例",
  },
  api_definition: {
    document: {
      name: "名称",
      value: "值",
      is_required: "是否必填",
      desc: "描述",
      type: "类型",
      default_value: "默认值",
    },
    body: {
      json_format_error: "JSON格式错误",
    },
    case_name: "用例名称",
    case_no_permission: "无编辑用例的权限",
  },
  home: {
    table: {
      index: "序号",
      scenario: "场景名称",
      task_type: "任务类型",
      run_rule: "运行规则",
      task_status: "任务状态",
      next_execution_time: "下次执行时间",
      create_user: "创建人",
      update_time: "更新时间",
      case_pass: "用例通过率",
      scenario_pass: "场景通过率",
    },
    case: {
      index: "排名",
      case_name: "用例名称",
      case_type: "用例类型",
      test_plan: "所属测试计划",
      failure_times: "失败次数",
    },
    new_case: {
      index: "ID",
      api_name: "接口名称",
      path: "路径",
      api_status: "状态",
      update_time: "更新时间",
      relation_case: "关联CASE",
      relation_scenario: "关联场景"
    },
  },
  automation:{
    project_no_permission: "当前操作人无此步骤的操作权限",
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

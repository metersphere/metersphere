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

import el from 'element-ui/lib/locale/lang/zh-CN'; // 加载element的内容
import fu from 'fit2cloud-ui/src/locale/lang/zh-CN'; // 加载fit2cloud的内容
import mf from 'metersphere-frontend/src/i18n/lang/zh-CN';

const message = {
  api_case: {
    please_add_api_case: '请先添加接口用例',
    mix_enable: '优先使用当前场景变量，没有则使用原场景变量',
  },
  api_definition: {
    debug_pool_warning: '调用资源池执行失败，请检查资源池是否配置正常',
    document: {
      name: '名称',
      value: '值',
      is_required: '是否必填',
      desc: '描述',
      type: '类型',
      default_value: '默认值',
    },
    copy_data_from_other_version: '复制版本数据',
    copy_data_from_other_version_tips: '名称相同的用例和Mock期望会进行强制覆盖!',
    body: {
      json_format_error: 'JSON格式错误',
    },
    case_name: '用例名称',
    case_no_permission: '无编辑用例的权限',
    view_case: '查看用例',
    view_scenario: '查看场景',
    case_is: '用例被',
    scenario_is: '场景被',
    scenario_count: '{0}个[场景]',
    plan_count: '{0}个[测试计划]',
    case_is_referenced: '有{0}个用例存在引用关系',
    scenario_is_referenced: '有{0}个场景存在引用关系',
    request: {
      auto_redirects: '自动重定向',
    },
  },
  home: {
    dashboard: {
      public: {
        default_version: '默认最新版本',
        no_data: '暂无数据',
        load_error: '加载失败',
        this_week: '本周',
        fake_error: '误报',
        executed_times_in_week: '本周执行次数',
        executed_times: '历史执行总次数',
        covered: '已覆盖',
        not_covered: '未覆盖',
        executed: '已执行',
        not_executed: '未执行',
        pass: '已通过',
        not_pass: '未通过',
        completed: '已完成',
        underway: '进行中',
        prepared: '未开始',
        running: '运行中',
        execute_complete: '运行完成',
        not_run: '未运行',
      },
      api: {
        title: '接口数量统计',
        api_total: '接口数量',
        covered_rate: '接口覆盖率',
        completed_rate: '接口完成率',
      },
      api_case: {
        title: '接口用例数量统计',
        api_case_total: '接口用例数量',
        covered_rate: '接口覆盖率',
        executed_rate: '用例执行率',
        pass_rate: '用例通过率',
      },
      scenario: {
        title: '场景用例数量统计',
        scenario_total: '场景用例数量',
        covered_rate: '接口覆盖率',
        executed_rate: '场景执行率',
        pass_rate: '执行通过率',
      },
      scenario_schedule: {
        title: '场景定时任务数量统计',
        running_count: '当前运行数',
        scenario_schedule_total: '场景定时任务数量',
        pass_rate: '运行通过率',
      },
    },
    table: {
      index: '序号',
      scenario: '场景名称',
      task_type: '任务类型',
      run_rule: '运行规则',
      task_status: '任务状态',
      next_execution_time: '下次执行时间',
      create_user: '创建人',
      update_time: '更新时间',
      case_pass: '用例通过率',
      scenario_pass: '场景通过率',
    },
    case: {
      index: '排名',
      case_name: '用例名称',
      case_type: '用例类型',
      test_plan: '所属测试计划',
      failure_times: '失败次数',
    },
    new_case: {
      index: 'ID',
      api_name: '接口名称',
      path: '路径',
      api_status: '状态',
      update_time: '更新时间',
      relation_case: '关联CASE',
      relation_scenario: '关联场景',
    },
  },
  automation: {
    project_no_permission: '当前操作人无此步骤的操作权限',
  },
};

export default {
  ...el,
  ...fu,
  ...mf,
  ...message,
};

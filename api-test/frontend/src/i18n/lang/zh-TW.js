import el from 'element-ui/lib/locale/lang/zh-TW';
import fu from 'fit2cloud-ui/src/locale/lang/zh-TW'; // 加载fit2cloud的内容
import mf from 'metersphere-frontend/src/i18n/lang/zh-TW';

const message = {
  api_case: {
    please_add_api_case: '请先添加接口用例',
    mix_enable: '優先使用當前場景變量，沒有則使用原場景變量',
  },
  api_definition: {
    debug_pool_warning: '調用資源池執行失敗，請檢查資源池是否配置正常',
    document: {
      name: '名稱',
      value: '值',
      is_required: '是否必填',
      desc: '描述',
      type: '類型',
      default_value: '默認值',
    },
    copy_data_from_other_version: '複製版本數據',
    copy_data_from_other_version_tips: '名稱相同的用例和Mock期望會進行強制覆蓋!',
    body: {
      json_format_error: 'JSON格式錯誤',
    },
    case_name: '用例名稱',
    case_no_permission: '無編輯用例的權限',
    view_case: '查看用例',
    view_scenario: '查看場景',
    case_is: '用例被',
    scenario_is: '場景被',
    scenario_count: '{0}個[場景]',
    plan_count: '{0}個[测试計劃]',
    case_is_referenced: '有{0}個用例存在引用關係',
    scenario_is_referenced: '有{0}個場景存在引用關係',
    request: {
      auto_redirects: '自動重定向',
    },
  },
  home: {
    dashboard: {
      public: {
        default_version: '默認最新版本',
        no_data: '暫無數據',
        load_error: '加載失敗',
        this_week: '本週',
        fake_error: '誤報',
        executed_times_in_week: '本週執行次數',
        executed_times: '歷史執行總次數',
        covered: '已覆蓋',
        not_covered: '未覆蓋',
        executed: '已執行',
        not_executed: '未執行',
        pass: '已通過',
        not_pass: '未通過',
        completed: '已完成',
        underway: '進行中',
        prepared: '未開始',
        execute_complete: '運行完成',
        running: '運行中',
        not_run: '未運行',
      },
      api: {
        title: '接口數量統計',
        api_total: '接口數量',
        covered_rate: '接口覆蓋率',
        completed_rate: '接口完成率',
      },
      api_case: {
        title: '接口用例數量統計',
        api_case_total: '接口用例數量',
        covered_rate: '接口覆蓋率',
        executed_rate: '用例執行率',
        pass_rate: '用例通過率',
      },
      scenario: {
        title: '場景用例數量統計',
        scenario_total: '場景用例數量',
        covered_rate: '接口覆蓋率',
        executed_rate: '場景執行率',
        pass_rate: '執行通過率',
      },
      scenario_schedule: {
        title: '場景定時任務數量統計',
        running_count: '當前運行數',
        scenario_schedule_total: '場景定時任務數量',
        pass_rate: '運行通過率',
      },
    },
    table: {
      index: '序號',
      scenario: '場景名稱',
      task_type: '任務類型',
      run_rule: '運行規則',
      task_status: '任務狀態',
      next_execution_time: '下次執行時間',
      create_user: '創建人',
      update_time: '更新時間',
      case_pass: '用例通過率',
      scenario_pass: '場景通過率',
    },
    case: {
      index: '排名',
      case_name: '用例名稱',
      case_type: '用例類型',
      test_plan: '所屬測試計劃',
      failure_times: '失敗次數',
    },
    new_case: {
      index: 'ID',
      api_name: '接口名稱',
      path: '路徑',
      api_status: '狀態',
      update_time: '更新時間',
      relation_case: '關聯CASE',
      relation_scenario: '關聯場景',
    },
  },
  automation: {
    project_no_permission: '當前人操作無此步驟的操作權限',
  },
};

export default {
  ...el,
  ...fu,
  ...mf,
  ...message,
};

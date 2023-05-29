import el from 'metersphere-frontend/src/i18n/lang/ele-en-US';
import fu from 'fit2cloud-ui/src/locale/lang/en_US'; // 加载fit2cloud的内容
import mf from 'metersphere-frontend/src/i18n/lang/en-US';

const message = {
  api_case: {
    please_add_api_case: 'Please add api case',
    mix_enable: 'The current scene variable is used first, and the original scene variable is used if there is no',
  },
  api_definition: {
    debug_pool_warning:
      'Failed to call the resource pool, please check whether the configuration of the resource pool is normal',
    document: {
      name: 'name',
      value: 'value',
      is_required: 'Is it required',
      desc: 'Description',
      type: 'Type',
      default_value: 'Default value',
    },
    copy_data_from_other_version: 'Copy data from other version',
    copy_data_from_other_version_tips:
      'Use cases with the same name and mock expectations will be forcibly overwritten!',
    body: {
      json_format_error: 'JSON format error',
    },
    case_name: 'Case name',
    case_no_permission: 'No permission to edit case',
    view_case: 'View case',
    view_scenario: 'View scenario',
    case_is: 'Case is',
    scenario_is: 'Scenario is',
    scenario_count: '{0} [scenario]',
    plan_count: '{0} [test plan]',
    case_is_referenced: '{0} cases have reference relationships',
    scenario_is_referenced: '{0} scenarios have reference relationships',
    request: {
      auto_redirects: 'Auto redirects',
    },
    document_tooltip: 'Only valid for displayed parameters',
  },
  filters: {
    schedule: {
      open: 'Open',
      close: 'Closed',
      unset: 'Unset',
    },
  },
  home: {
    dashboard: {
      public: {
        default_version: 'Default version data',
        no_data: 'No data',
        load_error: 'Load error',
        this_week: 'Week',
        fake_error: 'FakeError',
        executed_times_in_week: 'Executed in week',
        executed_times: 'Executed in history',
        covered: 'Covered',
        not_covered: 'Not covered',
        executed: 'Executed',
        not_executed: 'not executed',
        pass: 'Pass',
        not_pass: 'Not pass',
        completed: 'Completed',
        underway: 'Underway',
        prepared: 'Prepared',
        running: 'Running',
        execute_complete: 'Execute_complete',
        not_run: 'Not run',
      },
      api: {
        title: 'Api amount',
        api_total: 'Total',
        covered_rate: 'Api covered rate',
        completed_rate: 'Completed rate',
        executed_rate: 'Executed rate',
        pass_rate: 'Pass rate',
      },
      api_case: {
        title: 'Case amount',
        api_case_total: 'Total',
        covered_rate: 'Api covered rate',
        executed_rate: 'Executed rate',
        pass_rate: 'Pass rate',
      },
      scenario: {
        title: 'Scenario amount',
        scenario_total: 'Total',
        covered_rate: 'Api covered rate',
        executed_rate: 'Executed rate',
        pass_rate: 'Pass rate',
      },
      scenario_schedule: {
        title: 'Schedule scenario amount',
        running_count: 'Running count',
        scenario_schedule_total: 'Total',
        pass_rate: 'Pass rate',
      },
    },
    table: {
      index: 'Index',
      scenario: 'Scenario',
      task_type: 'Task Type',
      run_rule: 'Rule',
      task_status: 'Status',
      next_execution_time: 'Next Execution Time',
      create_user: 'Creator',
      update_time: 'Update time',
      case_pass: 'Case pass rate',
      scenario_pass: 'Scenario pass rate',
    },
    case: {
      index: 'Ranking',
      case_name: 'Case Name',
      case_type: 'Case Type',
      test_plan: 'Test Plan',
      failure_times: 'Failure times',
    },
    new_case: {
      index: 'ID',
      api_name: 'Api Name',
      path: 'path',
      api_status: 'Api status',
      update_time: 'Update time',
      relation_case: 'Relation CASE',
      relation_scenario: 'Relation Scenario',
    },
  },
  automation: {
    project_no_permission: 'The current person does not have the operation permission for this step',
    document_validity_msg: 'The file has been modified, please re-upload',
    scenario_step_ref_message:
      'The current number of selection requests [{0}] may cause page loading exceptions, whether to continue?',
    case_message: 'Please select a case',
    scenario_message: 'Please select a scene',
  },
};
export default {
  ...el,
  ...fu,
  ...mf,
  ...message,
};

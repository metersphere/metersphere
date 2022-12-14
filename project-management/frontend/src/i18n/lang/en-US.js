import el from "element-ui/lib/locale/lang/en";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  pj: {
    environment_import_repeat_tip: "(Environment configuration with the same name filtered {0})",
    check_third_project_success: "inspection passed",
    api_run_pool_title: 'Interface execution resource pool',
  },
  file_manage: {
    my_file: 'My File',
    update_user: 'Update User',
    all_file: 'All File',
    file_download: 'Download',
    batch_delete: 'Batch Delete',
    batch_move: 'Batch Move',
    batch_download: 'Batch Download'
  },
  pj_custom_field: {
    copy: "Copy",
    delete: "Delete"
  },
  pj_app_manage: {
    timing_clean_ui_report: "Regularly clean up ui report",
  },
  custom_template: {
    api_template: "Api Template",
    base_fields: 'Base fields',
    template_name: 'Template name',
    selected_custom_fields: 'Custom fields to be selected'
  },
  pj_batch_delete: {
    error_library: "Confirm batch delete error library"
  },
  project_version: {
    version_time: 'Version cycle',
  },
  environment: {
    export_variable_tip : "Export interface test variables",
    need_expire_time : "Please enter an expiration time",
    need_relevance_ui_scenario : "Please associate the login scenario",
    view_ui_relevane : "View Relevane",
    cancel_ui_relevane : "Relevant",
    re_ui_relevane : "Relevane",
    relevance_ui : "Relevance login scene/command",
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


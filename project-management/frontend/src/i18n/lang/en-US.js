import el from "element-ui/lib/locale/lang/en";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  pj: {
    environment_import_repeat_tip: "(Environment configuration with the same name filtered {0})"
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
  }

}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


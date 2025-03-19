import el from "metersphere-frontend/src/i18n/lang/ele-en-US";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {
  performance_test: {
    report: {
      test_duration: '{0} hours {1} minutes {2} seconds',
      test_duration_tips: 'Execution Time：{0} hours {1} minutes {2} seconds',
    },
    max_current_threads_tips: 'Exceeded the maximum concurrent number of this node {0}',
    sync_scenario_no_permission_tips: 'No permission to create the scenario cannot perform synchronization',
    basic_config_file_limit_tip: 'Note: The maximum number of resource files is limited to 100, Too many resource files may be detrimental to your testing.',
    edit_performance_test_tips: 'No permission to edit test, please check it before operation',
    error_samples: 'Error samples',
    all_samples: 'All samples',
    response_3_samples: 'The first three pieces of data',
    cache_script: 'Cache script',
    upload_limit_size_warn: 'Upload file size cannot exceed {0} MB!',
  }
}
export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};


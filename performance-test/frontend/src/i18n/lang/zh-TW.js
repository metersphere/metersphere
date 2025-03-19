import el from "metersphere-frontend/src/i18n/lang/ele-zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW"

const message = {
  performance_test: {
    report: {
      test_duration: '{0}時{1}分{2}秒',
      test_duration_tips: '執行時長：{0}時{1}分{2}秒',
    },
    max_current_threads_tips: '超出此節點{0}最大並發數',
    sync_scenario_no_permission_tips: '沒有创建接口的權限無法執行同步',
    basic_config_file_limit_tip: '注：資源文件數最大限制為100個，資源文件太多可能不利於您的測試正常進行',
    edit_performance_test_tips: '沒有編輯性能測試的權限，請勾選後再操作',
    error_samples: '錯誤請求',
    all_samples: '所有請求',
    response_3_samples: '默认抽样前3个请求的响应数据',
    cache_script: '緩存編譯腳本',
    upload_limit_size_warn: '上傳文件大小不能超過 {0} MB!',
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

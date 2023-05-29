import el from "metersphere-frontend/src/i18n/lang/ele-zh-TW";
import fu from "fit2cloud-ui/src/locale/lang/zh-TW"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-TW";

const message = {
  pj: {
    environment_import_repeat_tip: "(已過濾同名稱的環境配置 {0})",
    check_third_project_success: "檢查通過",
    api_run_pool_title: "接口執行資源池",
    api_script_review: "接口腳本審核",
    api_script_review_tips: "接口用例包含腳本步驟時須指定用戶審核",
    reviewers: "審核人",
    load_test_script_review: "性能腳本審核",
    load_test_script_review_detail: "上傳性能測試腳本文件須指定用戶審核",
    script_warning:"腳本已啟用審核機制，無法在當前頁面測試腳本"
  },
  file_manage: {
    my_file: "我的文件",
    update_user: "更新人",
    all_file: "所有文件",
    file_download: "下載",
    batch_delete: "批量刪除",
    batch_move: "批量移动",
    batch_download: "批量下载",
  },
  pj_custom_field: {
    copy: "複製字段",
    delete: "刪除字段",
  },
  pj_app_manage: {
    timing_clean_ui_report: "定時清理UI測試報告",
  },
  custom_template: {
    api_template: "接口模版",
    base_fields: "基礎字段",
    template_name: "模版名稱",
    selected_custom_fields: "已選自定義字段",
    zentao: "禪道",
    zentao_default_name: "禪道-默認模版",
    tapd_default_name: "TAPD-默認模版",
    jira_default_name: "JIRA-默認模版",
    zentao_default_description: "禪道默認模版",
    tapd_default_description: "TAPD默認模版",
    jira_default_description: "JIRA默認模版",
  },
  pj_batch_delete: {
    error_library: "確定刪除誤報庫",
  },
  project_version: {
    version_time: "版本週期",
  },
  file: {
    file_path_placeholder: "根目錄前無需添加文件路徑分隔符: / ",
  },
  environment: {
    export_variable_tip: "導出接口測試變量",
    need_expire_time: "請輸入過期時間",
    need_relevance_ui_scenario: "請關聯登錄場景",
    view_ui_relevane: "查看關聯",
    cancel_ui_relevane: "取消關聯",
    re_ui_relevane: "重新關聯",
    relevance_ui: "關聯登錄場景/指令",
    get_env_failed: "跳轉環境被删除！",
  },
};

export default {
  ...el,
  ...fu,
  ...mf,
  ...message,
};

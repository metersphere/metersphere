import el from "element-ui/lib/locale/lang/zh-CN"; // 加载element的内容
import fu from "fit2cloud-ui/src/locale/lang/zh-CN"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-CN"

const message = {
  file_manage: {
    my_file: '我的文件',
    update_user: '更新人',
    all_file: '所有文件',
    file_download: '下载',
    batch_delete: '批量删除',
    batch_move: '批量移动',
    batch_download: '批量下载'
  },
  pj_custom_field: {
    copy: "复制字段",
    delete: "删除字段"
  },
  pj_app_manage: {
    timing_clean_ui_report: "定时清理UI测试报告",
  },
  custom_template: {
    api_template: "接口模版",
    base_fields: '基础字段',
    template_name: '模版名称',
    selected_custom_fields: '已选自定义字段'
  },
  pj_batch_delete: {
    error_library: "确定批量删除误报库"
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

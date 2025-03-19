import el from "metersphere-frontend/src/i18n/lang/ele-zh-CN"; // 加载element的内容
import fu from "fit2cloud-ui/src/locale/lang/zh-CN"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/zh-CN"

const message = {
  performance_test: {
    report: {
      test_duration: '{0}时{1}分{2}秒',
      test_duration_tips: '执行时长：{0}时{1}分{2}秒',
    },
    max_current_threads_tips: '超出此节点{0}最大并发数',
    sync_scenario_no_permission_tips: '没有创建接口的权限无法执行同步',
    basic_config_file_limit_tip: '注：资源文件数最大限制为100个，资源文件太多可能不利于您的测试正常进行',
    edit_performance_test_tips: '没有编辑性能测试的权限，请勾选后再操作',
    error_samples: '错误请求',
    all_samples: '所有请求',
    response_3_samples: '默认抽样前3个请求的响应数据',
    cache_script: '缓存编译脚本',
    upload_limit_size_warn: '上传文件大小不能超过 {0} MB!',
  }
}

export default {
  ...el,
  ...fu,
  ...mf,
  ...message
};

export function LOG_TYPE(_this) {
  let LOG_TYPE = [
    {id: 'CREATE', label: _this.$t('api_test.definition.request.create_info')},
    {id: 'DELETE', label: _this.$t('commons.delete')},
    {id: 'UPDATE', label: _this.$t('commons.update')},
    {id: 'IMPORT', label: _this.$t('api_test.api_import.label')},
    {id: 'EXPORT', label: _this.$t('commons.export')},
    {id: 'ASSOCIATE_CASE', label: _this.$t('test_track.review_view.relevance_case')},
    {id: 'ASSOCIATE_ISSUE', label: _this.$t('test_track.case.relate_issue')},
    {id: 'UN_ASSOCIATE_CASE', label: _this.$t('test_track.case.unlink')},
    {id: 'REVIEW', label: _this.$t('test_track.review_view.start_review')},
    {id: 'COPY', label: _this.$t('commons.copy')},
    {id: 'EXECUTE', label: _this.$t('api_test.automation.execute')},
    {id: 'CREATE_PRE_TEST', label: _this.$t('api_test.create_performance_test')},
    {id: 'SHARE', label: _this.$t('operating_log.share')},
    {id: 'LOGIN', label: _this.$t('commons.login')},
    {id: 'RESTORE', label: _this.$t('commons.reduction')},
    {id: 'DEBUG', label: _this.$t('api_test.request.debug')},
    {id: 'GC', label: _this.$t('api_test.automation.trash')},
    {id: 'BATCH_DEL', label: _this.$t('api_test.definition.request.batch_delete')},
    {id: 'BATCH_UPDATE', label: _this.$t('api_test.definition.request.batch_edit')},
    {id: 'BATCH_ADD', label: _this.$t('commons.batch_add')},
    {id: 'BATCH_RESTORE', label: "批量恢复"},
    {id: 'BATCH_GC', label: "批量回收"}
  ];
  return LOG_TYPE;
}

export function LOG_TYPE_MAP(_this) {
  let LOG_TYPE_MAP = new Map([
    ['CREATE', _this.$t('api_test.definition.request.create_info')],
    ['DELETE', _this.$t('commons.delete')],
    ['UPDATE', _this.$t('commons.update')],
    ['IMPORT', _this.$t('api_test.api_import.label')],
    ['EXPORT', _this.$t('commons.export')],
    ['ASSOCIATE_CASE', _this.$t('test_track.review_view.relevance_case')],
    ['ASSOCIATE_ISSUE', _this.$t('test_track.case.relate_issue')],
    ['UN_ASSOCIATE_CASE', _this.$t('test_track.case.unlink')],
    ['REVIEW', _this.$t('test_track.review_view.start_review')],
    ['COPY', _this.$t('commons.copy')],
    ['EXECUTE', _this.$t('api_test.automation.execute')],
    ['CREATE_PRE_TEST', _this.$t('api_test.create_performance_test')],
    ['SHARE', _this.$t('operating_log.share')],
    ['LOGIN', _this.$t('commons.login')],
    ['RESTORE', _this.$t('commons.reduction')],
    ['DEBUG', _this.$t('api_test.request.debug')],
    ['GC', _this.$t('api_test.automation.trash')],
    ['BATCH_DEL', _this.$t('api_test.definition.request.batch_delete')],
    ['BATCH_UPDATE', _this.$t('api_test.definition.request.batch_edit')],
    ['BATCH_ADD', _this.$t('commons.batch_add')],
    ['BATCH_RESTORE', "批量恢复"],
    ['BATCH_GC', "批量回收"],
  ]);
  return LOG_TYPE_MAP;
}

export const sysList = [
  {
    label: "测试跟踪", value: "测试跟踪", children: [
      {label: "测试用例", value: "测试用例", leaf: true},
      {label: "用例评审", value: "用例评审", leaf: true},
      {label: "测试计划", value: "测试计划", leaf: true},
      {label: "缺陷管理", value: "缺陷管理", leaf: true},
      {label: "报告", value: "报告", leaf: true}]
  },
  {
    label: "接口测试", value: "api", children: [
      {label: "接口定义", value: "接口定义", leaf: true},
      {label: "接口自动化", value: "接口自动化", leaf: true},
      {label: "测试报告", value: "测试报告", leaf: true}]
  },
  {
    label: "性能测试", value: "性能测试", children: [
      {label: "性能测试", value: "性能测试", leaf: true},
      {label: "性能测试报告", value: "性能测试报告", leaf: true}]
  },
  {
    label: "系统设置", value: "系统设置", children: [
      {label: "系统-用户", value: "系统-用户", leaf: true},
      {label: "工作空间", value: "工作空间", leaf: true},
      {label: "系统-测试资源池", value: "系统-测试资源池", leaf: true},
      {label: "系统-系统参数设置", value: "系统-系统参数设置", leaf: true},
      {label: "系统-配额管理", value: "系统-配额管理", leaf: true},
      {label: "系统-授权管理", value: "系统-授权管理", leaf: true},
      {label: "工作空间-服务集成", value: "工作空间-服务集成", leaf: true},
      {label: "工作空间-消息设置", value: "工作空间-消息设置", leaf: true},

      {label: "工作空间-成员", value: "工作空间-成员", leaf: true},
      {label: "项目-项目管理", value: "项目-项目管理", leaf: true},
      {label: "工作空间-模版设置", value: "工作空间-模版设置", leaf: true},
      {label: "工作空间-项目管理", value: "工作空间-项目管理", leaf: true},
      {label: "项目-项目管理", value: "项目-项目管理", leaf: true},
      {label: "项目-成员", value: "项目-成员", leaf: true},
      {label: "工作空间-成员", value: "工作空间-成员", leaf: true},

      {label: "項目-JAR包管理", value: "項目-JAR包管理", leaf: true},
      {label: "项目-环境设置", value: "项目-环境设置", leaf: true},
      {label: "项目-文件管理", value: "项目-文件管理", leaf: true},
      {label: "个人信息-个人设置", value: "个人信息-个人设置", leaf: true},
      {label: "个人信息-API Keys", value: "个人信息-API Keys", leaf: true}
    ]
  },
];

export function getUrl(d) {
  let url = "/#";
  let resourceId = d.sourceId;
  if (resourceId && (resourceId.startsWith("\"") || resourceId.startsWith("["))) {
    resourceId = JSON.parse(d.sourceId);
  }
  if (resourceId instanceof Array) {
    if (resourceId.length === 1) {
      resourceId = resourceId[0];
    } else {
      return url;
    }
  }
  switch (d.operModule) {
    case "接口自动化" || "Api automation" || "接口自動化":
      url += "/api/automation?resourceId=" + resourceId;
      break;
    case "测试计划" || "測試計劃" || "Test plan":
      url += "/track/plan/view/" + resourceId;
      break;
    case "用例评审" || "Case review" || "用例評審":
      url += "/track/review/view/" + resourceId;
      break;
    case "缺陷管理" || "Defect management":
      url += "/track/issue";
      break;
    case "SWAGGER_TASK" :
      url += "/api/definition";
      break;
    case "接口定义" || "接口定義" || "Api definition":
      url += "/api/definition?resourceId=" + resourceId;
      break;
    case "接口定义用例" || "接口定義用例" || "Api definition case":
      url += "/api/definition?caseId=" + resourceId;
      break;
    case "测试报告" || "測試報告" || "Test Report":
      url += "/api/automation/report";
      break;
    case "性能测试报告" || "性能測試報告" || "Performance test report" :
      url += "/performance/report/all";
      break;
    case "性能测试" || "性能測試" || "Performance test" :
      url += "/performance/test/edit/" + resourceId;
      break;
    case "测试用例" || "測試用例" || "Test case":
      url += "/track/case/all?resourceId=" + resourceId;
      break;
    case "系统-用户" || "系统-用户" || "System user":
      url += "/setting/user";
      break;
    case "系统-组织" || "系統-組織" || "System organization":
      url += "/setting/organization";
      break;
    case "工作空间" || "系统-工作空间" || "workspace" :
      url += "/setting/systemworkspace";
      break;
    case "用户组与权限" || "用戶組與權限" || "Group" :
      url += "/setting/usergroup";
      break;
    case "系统-测试资源池" || "系统-測試資源池" || "System test resource" :
      url += "/setting/testresourcepool";
      break;
    case "系统-系统参数设置" || "系统-系統參數設置" || "System parameter setting" :
      url += "/setting/systemparametersetting";
      break;
    case "工作空间-成员" || "工作空間-成員" || "Workspace member" :
      url += "/setting/member";
      break;
    case "项目-项目管理" || "項目-項目管理" || "Project project manager" :
      url += "/setting/project/:type";
      break;
    case "项目-环境设置" || "項目-環境設置" || "Project environment setting" :
      url += "/project/env";
      break;
    case "工作空间-模版设置-自定义字段" || "工作空間-模版設置-自定義字段" || "Workspace template settings field" :
      url += "/setting/workspace/template/field";
      break;
    case "工作空间-模版设置-用例模版" || "工作空間-模版設置-用例模板" || "Workspace template settings case" :
      url += "/setting/workspace/template/case";
      break;
    case "工作空间-模版设置-缺陷模版" || "工作空間-模版設置-缺陷模板" || "Workspace template settings issue" :
      url += "/setting/workspace/template/issues";
      break;
    case "项目-成员" || "項目-成員" || "Project member" :
      url += "/project/member";
      break;
    default:
      break;

  }
  return url;
}

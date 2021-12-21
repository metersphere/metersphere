import i18n from "@/i18n/i18n";

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
    {id: 'BATCH_RESTORE', label: _this.$t('commons.batch_restore')},
    {id: 'BATCH_GC', label: _this.$t('commons.batch_gc')}
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
    ['BATCH_RESTORE', _this.$t('commons.batch_restore')],
    ['BATCH_GC', _this.$t('commons.batch_gc')],
  ]);
  return LOG_TYPE_MAP;
}

export function SYSLIST(){
  let sysList = [
    {
      label: i18n.t('test_track.test_track'), value: i18n.t('test_track.test_track'), children: [
        {label: i18n.t('permission.project_track_case.name'), value: i18n.t('permission.project_track_case.name'), leaf: true},
        {label: i18n.t('test_track.review.test_review'), value: i18n.t('test_track.review.test_review'), leaf: true},
        {label: i18n.t('test_track.plan.test_plan'), value: i18n.t('test_track.plan.test_plan'), leaf: true},
        {label: i18n.t('test_track.issue.issue_management'), value: i18n.t('test_track.issue.issue_management'), leaf: true},
        {label: i18n.t('commons.report'), value: i18n.t('commons.report'), leaf: true}]
    },
    {
      label: i18n.t('commons.api'), value: i18n.t('commons.api'), children: [
        {label: i18n.t('workstation.table_name.api_definition'), value: i18n.t('workstation.table_name.api_definition'), leaf: true},
        {label: i18n.t('workstation.table_name.api_automation'), value: i18n.t('workstation.table_name.api_automation'), leaf: true},
        {label: i18n.t('permission.project_api_report.name'), value: i18n.t('permission.project_api_report.name'), leaf: true}]
    },
    {
      label: i18n.t('workstation.table_name.performance'), value: i18n.t('workstation.table_name.performance'), children: [
        {label: i18n.t('workstation.table_name.performance'), value: i18n.t('workstation.table_name.performance'), leaf: true},
        {label: i18n.t('report.load_test_report'), value: i18n.t('report.load_test_report'), leaf: true}]
    },
    {
      label: i18n.t('commons.system_setting'), value: i18n.t('commons.system_setting'), children: [
        {label: i18n.t('commons.system')+"-"+i18n.t('commons.user'), value: i18n.t('commons.system')+"-"+i18n.t('commons.user'), leaf: true},
        {label: i18n.t('commons.system')+"-"+i18n.t('commons.test_resource_pool'), value: i18n.t('commons.system')+"-"+i18n.t('commons.test_resource_pool'), leaf: true},
        {label: i18n.t('commons.system')+"-"+i18n.t('commons.system_parameter_setting'), value: i18n.t('commons.system')+"-"+i18n.t('commons.system_parameter_setting'), leaf: true},
        {label: i18n.t('commons.system')+"-"+i18n.t('commons.quota'), value: i18n.t('commons.system')+"-"+i18n.t('commons.quota'), leaf: true},
        {label: i18n.t('commons.system')+"-"+i18n.t('license.title'), value: i18n.t('commons.system')+"-"+i18n.t('license.title'), leaf: true},

        {label: i18n.t('commons.workspace'), value: i18n.t('commons.workspace'), leaf: true},
        {label: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_service.name'), value: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_service.name'), leaf: true},
        {label: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_message.name'), value: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_message.name'), leaf: true},
        {label: i18n.t('commons.workspace')+"-"+i18n.t('permission.project_user.name'), value: i18n.t('commons.workspace')+"-"+i18n.t('permission.project_user.name'), leaf: true},
        {label: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_template.name'), value: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_template.name'), leaf: true},
        {label: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_project_manager.name'), value: i18n.t('commons.workspace')+"-"+i18n.t('permission.workspace_project_manager.name'), leaf: true},

        {label: i18n.t('commons.project')+"-"+i18n.t('project.manager'), value: i18n.t('commons.project')+"-"+i18n.t('project.manager'), leaf: true},
        {label: i18n.t('commons.project')+"-"+i18n.t('permission.project_user.name'), value: i18n.t('commons.project')+"-"+i18n.t('permission.project_user.name'), leaf: true},
        {label: i18n.t('commons.project')+"-"+i18n.t('api_test.jar_config.jar_manage'), value: i18n.t('commons.project')+"-"+i18n.t('api_test.jar_config.jar_manage'), leaf: true},
        {label: i18n.t('commons.project')+"-"+i18n.t('permission.workspace_project_environment.name'), value: i18n.t('commons.project')+"-"+i18n.t('permission.workspace_project_environment.name'), leaf: true},
        {label: i18n.t('commons.project')+"-"+i18n.t('permission.project_file.name'), value: i18n.t('commons.project')+"-"+i18n.t('permission.project_file.name'), leaf: true},

        {label: i18n.t('commons.personal_information')+"-"+i18n.t('commons.personal_setting'), value: i18n.t('commons.personal_information')+"-"+i18n.t('commons.personal_setting'), leaf: true},
        {label: i18n.t('commons.personal_information')+"-API Keys", value: i18n.t('commons.personal_information')+"-API Keys", leaf: true}
      ]
    },
  ];
  return sysList;
}


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

  if (d.operModule) {
    if (d.operModule === "接口自动化" || d.operModule === "Api automation" || d.operModule === "接口自動化") {
      url += "/api/automation?resourceId=" + resourceId;
    }
    if (d.operModule === "测试计划" || d.operModule === "Test plan" || d.operModule === "測試計劃") {
      url += "/track/plan/view/" + resourceId;
    }
    if (d.operModule === "用例评审" || d.operModule === "Case review" || d.operModule === "用例評審") {
      url += "/track/review/view/" + resourceId;
    }
    if (d.operModule === "缺陷管理" || d.operModule === "Defect management") {
      url += "/track/issue";
    }
    if (d.operModule === "SWAGGER_TASK") {
      url += "/api/definition";
    }
    if (d.operModule === "接口定义" || d.operModule === "Api definition" || d.operModule === "接口定義") {
      url += "/api/definition?resourceId=" + resourceId;
    }
    if (d.operModule === "接口定义用例" || d.operModule === "Api definition case" || d.operModule === "接口定義用例") {
      url += "/api/definition?resourceId=" + resourceId;
    }
    if (d.operModule === "测试报告" || d.operModule === "Test Report" || d.operModule === "測試報告") {
      url += "/api/automation/report";
    }
    if (d.operModule === "性能测试报告" || d.operModule === "Performance test report" || d.operModule === "性能測試報告") {
      url += "/performance/report/all";
    }
    if (d.operModule === "性能测试" || d.operModule === "Performance test" || d.operModule === "性能測試") {
      url += "/performance/test/edit/" + resourceId;
    }
    if (d.operModule === "测试用例" || d.operModule === "Test case" || d.operModule === "測試用例") {
      url += "/track/case/all?resourceId=" + resourceId;
    }
    if (d.operModule === "系统-用户" || d.operModule === "System user" || d.operModule === "系统-用户") {
      url += "/setting/user";
    }
    if (d.operModule === "系统-组织" || d.operModule === "System organization" || d.operModule === "系統-組織") {
      url += "/setting/organization";
    }
    if (d.operModule === "工作空间" || d.operModule === "workspace" || d.operModule === "系统-工作空间") {
      url += "/setting/systemworkspace";
    }
    if (d.operModule === "用户组与权限" || d.operModule === "Group" || d.operModule === "用戶組與權限") {
      url += "/setting/usergroup";
    }
    if (d.operModule === "系统-测试资源池" || d.operModule === "System test resource" || d.operModule === "系统-測試資源池") {
      url += "/setting/testresourcepool";
    }
    if (d.operModule === "系统-系统参数设置" || d.operModule === "System parameter setting" || d.operModule === "系统-系統參數設置") {
      url += "/setting/systemparametersetting";
    }
    if (d.operModule === "工作空间-成员" || d.operModule === "Workspace member" || d.operModule === "工作空間-成員") {
      url += "/setting/member";
    }
    if (d.operModule === "项目-项目管理" || d.operModule === "Project project manager" || d.operModule === "項目-項目管理") {
      url += "/setting/project/:type";
    }
    if (d.operModule === "项目-环境设置" || d.operModule === "Project environment setting" || d.operModule === "項目-環境設置") {
      url += "/project/env";
    }
    if (d.operModule === "工作空间-模版设置-自定义字段" || d.operModule === "Workspace template settings field" || d.operModule === "工作空間-模版設置-自定義字段") {
      url += "/setting/workspace/template/field";
    }
    if (d.operModule === "工作空间-模版设置-用例模版" || d.operModule === "Workspace template settings case" || d.operModule === "工作空間-模版設置-用例模版") {
      url += "/setting/workspace/template/case";
    }
    if (d.operModule === "工作空间-模版设置-缺陷模版" || d.operModule === "Workspace template settings issue" || d.operModule === "工作空間-模版設置-缺陷模版") {
      url += "/setting/workspace/template/issue";
    }
    if (d.operModule === "项目-成员" || d.operModule === "Project member" || d.operModule === "項目-成員") {
      url += "/project/member";
    }
  }
  return url;
}

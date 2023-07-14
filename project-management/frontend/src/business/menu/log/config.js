import i18n from "@/i18n";

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
    {id: 'BATCH_GC', label: _this.$t('commons.batch_gc')},
    {id: 'REQUEST', label: _this.$t('operating_log.request_record')},
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
    ['REQUEST', _this.$t('operating_log.request_record')],
  ]);
  return LOG_TYPE_MAP;
}

export function LOG_MODULE_MAP(_this) {
  let LOG_MODULE_MAP = new Map([
    ['SYSTEM_PARAMETER_SETTING', _this.$t('operating_log.system_parameter_setting')],
    ['SYSTEM_TEST_RESOURCE', _this.$t('operating_log.system_test_resource')],
    ['SYSTEM_USER', _this.$t('operating_log.system_user')],
    ['SYSTEM_WORKSPACE', _this.$t('operating_log.system_workspace')],
    ['WORKSPACE_TEMPLATE_SETTINGS', _this.$t('operating_log.project_template_settings')],
    ['WORKSPACE_MESSAGE_SETTINGS', _this.$t('operating_log.project_message_settings')],
    ['WORKSPACE_TEMPLATE_SETTINGS_FIELD', _this.$t('operating_log.project_template_settings_field')],
    ['WORKSPACE_TEMPLATE_SETTINGS_ISSUE', _this.$t('operating_log.project_template_settings_issue')],
    ['WORKSPACE_SERVICE_INTEGRATION', _this.$t('operating_log.workspace_service_integration')],
    ['WORKSPACE_TEMPLATE_SETTINGS_CASE', _this.$t('operating_log.project_template_settings_case')],
    ['WORKSPACE_MEMBER', _this.$t('operating_log.workspace_member')],
    ['API_AUTOMATION', _this.$t('operating_log.api_automation')],
    ['API_AUTOMATION_SCHEDULE', _this.$t('operating_log.api_automation_schedule')],
    ['API_AUTOMATION_REPORT', _this.$t('operating_log.api_automation_report')],
    ['API_DEFINITION', _this.$t('operating_log.api_definition')],
    ['API_DEFINITION_CASE', _this.$t('operating_log.api_definition_case')],
    ['TRACK_TEST_PLAN', _this.$t('operating_log.track_test_plan')],
    ['TRACK_TEST_PLAN_SCHEDULE', _this.$t('operating_log.track_test_plan_schedule')],
    ['TRACK_BUG', _this.$t('operating_log.track_bug')],
    ['TRACK_TEST_CASE_REVIEW', _this.$t('operating_log.track_test_case_review')],
    ['TRACK_TEST_CASE', _this.$t('operating_log.track_test_case')],
    ['TRACK_REPORT', _this.$t('operating_log.track_report')],
    ['AUTH_TITLE', _this.$t('operating_log.auth_title')],
    ['PROJECT_PROJECT_JAR', _this.$t('operating_log.project_project_jar')],
    ['PROJECT_ENVIRONMENT_SETTING', _this.$t('operating_log.project_environment_setting')],
    ['PROJECT_PROJECT_MANAGER', _this.$t('operating_log.project_project_manager')],
    ['PROJECT_FILE_MANAGEMENT', _this.$t('operating_log.project_file_management')],
    ['PROJECT_TEMPLATE_MANAGEMENT', _this.$t('operating_log.project_template_settings_issue')],
    ['PROJECT_PROJECT_MEMBER', _this.$t('operating_log.project_project_member')],
    ['PERSONAL_INFORMATION_PERSONAL_SETTINGS', _this.$t('operating_log.personal_information_personal_settings')],
    ['PERSONAL_INFORMATION_APIKEYS', _this.$t('operating_log.personal_information_apikeys')],
    ['GROUP_PERMISSION', _this.$t('operating_log.group_permission')],
    ['PERFORMANCE_TEST_REPORT', _this.$t('operating_log.performance_test_report')],
    ['PERFORMANCE_TEST', _this.$t('operating_log.performance_test')],
    ['ERROR_REPORT_LIBRARY', _this.$t('operating_log.error_report_library')],
    ['SYSTEM_QUOTA_MANAGEMENT', _this.$t('operating_log.system_quota_management')],
    ['ENTERPRISE_TEST_REPORT', _this.$t('operating_log.enterprise_test_report')],
    ['SYSTEM_AUTHORIZATION_MANAGEMENT', _this.$t('operating_log.system_authorization_management')],
    ['UI_ELEMENT', _this.$t('operating_log.ui_element')],
    ['UI_AUTOMATION', _this.$t('operating_log.ui_automation')],
    ['UI_AUTOMATION_SCHEDULE', _this.$t('operating_log.ui_automation_schedule')],
    ['UI_AUTOMATION_REPORT', _this.$t('operating_log.ui_automation_report')],
  ]);
  return LOG_MODULE_MAP;
}

export function PROJECTSYSLIST() {
  let projectsysList = [
    {
      label: i18n.t('commons.ui'), value: i18n.t('commons.ui'), license: true,
      children: [
        {
          label: i18n.t('ui.ui_element'),
          value: [i18n.t('ui.ui_element'), 'UI_ELEMENT'],
          leaf: true
        },
        {
          label: i18n.t('ui.ui_automation'),
          value: [i18n.t('ui.ui_automation'), 'UI_AUTOMATION'],
          leaf: true
        },
        {
          label: i18n.t('operating_log.ui_automation_schedule'),
          value: [i18n.t('operating_log.ui_automation_schedule'), 'UI_AUTOMATION_SCHEDULE'],
          leaf: true
        },
        {
          label: i18n.t('ui.report'),
          value: [i18n.t('ui.report'), 'UI_AUTOMATION_REPORT'],
          leaf: true
        },
      ]
    },
    {
      label: i18n.t('test_track.test_track'), value: i18n.t('test_track.test_track'), children: [
        {
          label: i18n.t('permission.project_track_case.name'),
          value: [i18n.t('permission.project_track_case.name'), 'TRACK_TEST_CASE'],
          leaf: true
        },
        {
          label: i18n.t('test_track.review.test_review'),
          value: [i18n.t('test_track.review.test_review'), 'TRACK_TEST_CASE_REVIEW'],
          leaf: true
        },
        {
          label: i18n.t('test_track.plan.test_plan'),
          value: [i18n.t('test_track.plan.test_plan'), 'TRACK_TEST_PLAN'],
          leaf: true
        },
        {
          label: i18n.t('operating_log.track_test_plan_schedule'),
          value: [i18n.t('operating_log.track_test_plan_schedule'), 'TRACK_TEST_PLAN_SCHEDULE'],
          leaf: true
        },
        {
          label: i18n.t('test_track.issue.issue_management'),
          value: [i18n.t('test_track.issue.issue_management'), 'TRACK_BUG'],
          leaf: true
        },
        {label: i18n.t('commons.report'), value: [i18n.t('commons.report'), 'TRACK_REPORT'], leaf: true}]
    },
    {
      label: i18n.t('commons.api'), value: i18n.t('commons.api'), children: [
        {
          label: i18n.t('workstation.table_name.api_definition'),
          value: [i18n.t('workstation.table_name.api_definition'), 'API_DEFINITION'],
          leaf: true
        },
        {
          label: i18n.t('operating_log.api_definition_case'),
          value: [i18n.t('operating_log.api_definition_case'), 'API_DEFINITION_CASE'],
          leaf: true
        },
        {
          label: i18n.t('workstation.table_name.api_automation'),
          value: [i18n.t('workstation.table_name.api_automation'), 'API_AUTOMATION'],
          leaf: true
        },
        {
          label: i18n.t('operating_log.api_automation_schedule'),
          value: [i18n.t('operating_log.api_automation_schedule'), 'API_AUTOMATION_SCHEDULE'],
          leaf: true
        },
        {
          label: i18n.t('permission.project_api_report.name'),
          value: [i18n.t('permission.project_api_report.name'), 'API_AUTOMATION_REPORT'],
          leaf: true
        }]
    },
    {
      label: i18n.t('workstation.table_name.performance'),
      value: i18n.t('workstation.table_name.performance'),
      children: [
        {
          label: i18n.t('workstation.table_name.performance'),
          value: [i18n.t('workstation.table_name.performance'), 'PERFORMANCE_TEST'],
          leaf: true
        },
        {
          label: i18n.t('report.load_test_report'),
          value: [i18n.t('report.load_test_report'), 'PERFORMANCE_TEST_REPORT'],
          leaf: true
        }]
    },

    {
      label: i18n.t('commons.project_setting'), value: i18n.t('commons.project_setting'), children: [
        {
          label: i18n.t('commons.project') + "-" + i18n.t('project.manager'),
          value: [i18n.t('commons.project') + "-" + i18n.t('project.manager'), 'PROJECT_PROJECT_MANAGER'],
          leaf: true
        },
        {
          label: i18n.t('commons.project') + "-" + i18n.t('permission.project_user.name'),
          value: [i18n.t('commons.project') + "-" + i18n.t('permission.project_user.name'), 'PROJECT_PROJECT_MEMBER'],
          leaf: true
        },
        {
          label: i18n.t('commons.project') + "-" + i18n.t('api_test.jar_config.jar_manage'),
          value: [i18n.t('commons.project') + "-" + i18n.t('api_test.jar_config.jar_manage'), 'PROJECT_PROJECT_JAR'],
          leaf: true
        },
        {
          label: i18n.t('commons.project') + "-" + i18n.t('permission.workspace_project_environment.name'),
          value: [i18n.t('commons.project') + "-" + i18n.t('permission.workspace_project_environment.name'), 'PROJECT_ENVIRONMENT_SETTING'],
          leaf: true
        },
        {
          label: i18n.t('commons.project') + "-" + i18n.t('permission.project_file.name'),
          value: [i18n.t('commons.project') + "-" + i18n.t('permission.project_file.name'), 'PROJECT_FILE_MANAGEMENT'],
          leaf: true
        },
        {
          label: i18n.t('commons.project') + "-" + i18n.t('permission.template.name'),
          value: [i18n.t('commons.project') + "-" + i18n.t('permission.template.name'), 'PROJECT_TEMPLATE_MANAGEMENT'],
          leaf: true
        },
        {
          label: i18n.t('commons.project') + "-" + i18n.t('permission.project_message.name'),
          value: [i18n.t('commons.project') + "-" + i18n.t('permission.project_message.name'), 'WORKSPACE_MESSAGE_SETTINGS'],
          leaf: true
        },
        {
          label: i18n.t('commons.project') + "-" + i18n.t('operating_log.group_permission'),
          value: [i18n.t('commons.project') + "-" + i18n.t('operating_log.group_permission'), 'GROUP_PERMISSION'],
          leaf: true
        },
      ]
    },
  ];
  return projectsysList;
}

export function getPermissionUrl(d, _this) {
  let url = "/#";
  let resourceId = d.sourceId;
  if (resourceId && (resourceId.startsWith("\"") || resourceId.startsWith("["))) {
    resourceId = JSON.parse(d.sourceId);
  }
  if (resourceId instanceof Array) {
    if (resourceId.length === 1) {
      resourceId = resourceId[0];
    } else {
      return [url, []];
    }
  }
  let moduleMap = LOG_MODULE_MAP(_this);
  let module = moduleMap.get(d.operModule) ? moduleMap.get(d.operModule) : d.operModule;
  let res;
  switch (module) {
    case "接口自动化" :
    case "Api automation" :
    case"接口自動化":
      res = [url + "/api/automation?resourceId=" + resourceId, ["PROJECT_API_SCENARIO:READ"]];
      break;
    case "测试计划" :
    case "測試計劃" :
    case "Test plan":
      res = [url + "/track/plan/view/" + resourceId, ["PROJECT_TRACK_PLAN:READ"]];
      break;
    case "用例评审" :
    case "Case review" :
    case "用例評審":
      res = [url + "/track/review/view/" + resourceId, ["PROJECT_TRACK_REVIEW:READ"]];
      break;
    case "缺陷管理" :
    case "Defect management":
      res = [url + "/track/issue/", ["PROJECT_TRACK_ISSUE:READ"]];
      break;
    case "SWAGGER_TASK" :
      res = [url + "/api/definition", ["PROJECT_API_DEFINITION:READ"]];
      break;
    case "接口定义" :
    case "接口定義" :
    case "Api definition":
      res = [url + "/api/definition?resourceId=" + resourceId, ["PROJECT_API_DEFINITION:READ"]];
      break;
    case "接口定义用例" :
    case "接口定義用例":
    case "Api definition case":
      res = [url + "/api/definition?caseId=" + resourceId, ["PROJECT_API_DEFINITION:READ"]];
      break;
    case "测试报告" :
    case "測試報告" :
    case "Test Report":
      res = [url + "/api/automation/report", ["PROJECT_API_REPORT:READ"]];
      break;
    case "性能测试报告" :
    case "性能測試報告" :
    case "Performance test report" :
      res = [url + "/performance/report/all", ["PROJECT_PERFORMANCE_REPORT:READ"]];
      break;
    case "性能测试" :
    case "性能測試" :
    case "Performance test" :
      res = [url + "/performance/test/edit/" + resourceId, ["PROJECT_PERFORMANCE_TEST:READ+EDIT"]];
      break;
    case "测试用例" :
    case "測試用例" :
    case "Test case":
      res = [url + "/track/case/all?resourceId=" + resourceId, ["PROJECT_TRACK_CASE:READ+EDIT"]];
      break;
    case "系统-用户":
    case "System user":
      res = [url + "/setting/user", ["SYSTEM_USER:READ"]];
      break;
    case "系统-组织" :
    case "系統-組織" :
    case "System organization":
      res = [url + "/setting/organization", ["SYSTEM_ORGANIZATION:READ"]];
      break;
    case "工作空间" :
    case "系统-工作空间" :
    case "workspace" :
      res = [url + "/setting/systemworkspace", ["SYSTEM_WORKSPACE:READ"]];
      break;
    case "用户组与权限" :
    case "用戶組與權限" :
    case "Group" :
      res = [url + "/setting/usergroup", ["SYSTEM_GROUP:READ"]];
      break;
    case "系统-测试资源池":
    case "系统-測試資源池" :
    case "System test resource" :
      res = [url + "/setting/testresourcepool", ["SYSTEM_TEST_POOL:READ"]];
      break;
    case "系统-系统参数设置":
    case "系统-系統參數設置" :
    case "System parameter setting" :
      res = [url + "/setting/systemparametersetting", ["SYSTEM_SETTING:READ"]];
      break;
    case "工作空间-成员" :
    case "工作空間-成員" :
    case "Workspace member" :
      res = [url + "/setting/member", ["WORKSPACE_USER:READ"]];
      break;
    case "项目-项目管理" :
    case "項目-項目管理" :
    case "Project project manager" :
      res = [url + "/setting/project/:type", ["WORKSPACE_PROJECT_MANAGER:READ"]];
      break;
    case "项目-环境设置" :
    case "項目-環境設置" :
    case "Project environment setting" :
      res = [url + "/project/env", ["WORKSPACE_PROJECT_ENVIRONMENT:READ"]];
      break;
    case "项目-模版设置-自定义字段" :
    case "項目-模版設置-自定義字段" :
    case "Project template settings field" :
      res = [url + "/project/template", ["PROJECT_TEMPLATE:READ"]];
      break;
    case "项目-成员":
    case "項目-成員" :
    case "Project member" :
      res = [url + "/project/member", ["PROJECT_USER:READ"]];
      break;
    default:
      res = [url, []];
      break;
  }
  return res;
}



import i18n from "@/i18n/i18n";

export function getOperation(operation) {
  return i18n.t('notice.operation.' + operation);
}

export function getResource(d) {
  switch (d.operation) {
    case "CASE_CREATE":
    case "CASE_UPDATE":
    case "CASE_DELETE":
      return "";
  }

  let resourceType = i18n.t('notice.resource.' + d.resourceType);
  if (!d.operation.startsWith('EXECUTE_')) {
    return resourceType;
  }
  switch (d.resourceType) {
    case "JENKINS_TASK" :
    case "PERFORMANCE_TEST_TASK" :
    case "API_AUTOMATION_TASK" :
      resourceType += i18n.t('notice.result.' + d.operation);
      break;
    case "API_DEFINITION_TASK" :
      resourceType = i18n.t('notice.api_case') + i18n.t('notice.result.' + d.operation);
      break;
    default:
      break;
  }
  return resourceType;
}

export function getUrl(d) {
  let url = "/#";
  switch (d.resourceType) {
    case "JENKINS_TASK" :
      // jenkins 跳转需要特殊处理
      try {
        let obj = JSON.parse(d.content);
        // 接口自动化，性能测试
        if (obj.reportUrl) {
          let s = obj.reportUrl.indexOf('#');
          url += obj.reportUrl.substring(s + 1);
        }
        // 接口用例
        else if (obj.caseStatus) {
          url += "/api/definition?caseId=" + d.resourceId;
        }
        // 测试计划
        else if (obj.url) {
          let s = obj.url.indexOf('#');
          url += obj.url.substring(s + 1);
        } else {
          url += "/track/plan/all";
        }
      } catch (e) {
        // jenkins 跳转需要特殊处理
        if (d.content.indexOf("接口用例") > -1) {
          url += "/api/definition?caseId=" + d.resourceId;
        } else if (d.content.indexOf("性能测试") > -1) {
          url += "/performance/test/edit/" + d.resourceId;
        } else if (d.content.indexOf("接口测试") > -1) {
          url += "/api/automation/report/view/" + d.resourceId;
        } else if (d.content.indexOf("测试计划运行") > -1) {
          url += "/track/plan/view/" + d.resourceId;
        } else {
          url += "/track/plan/all";
        }
      }

      break;
    case "TEST_PLAN_TASK" :
      url += "/track/plan/view/" + d.resourceId;
      break;
    case "REVIEW_TASK" :
      url += "/track/review/view/" + d.resourceId;
      break;
    case "DEFECT_TASK" :
      url += "/track/issue";
      break;
    case "SWAGGER_TASK" :
      url += "/api/definition";
      break;
    case "API_AUTOMATION_TASK" :
      url += "/api/automation?resourceId=" + d.resourceId;
      break;
    case "API_DEFINITION_TASK" :
      if (d.operation.startsWith('CASE_') || d.operation.startsWith('EXECUTE_')) {
        url += "/api/definition?caseId=" + d.resourceId;
      } else {
        url += "/api/definition?resourceId=" + d.resourceId;
      }
      break;
    case "API_HOME_TASK" :
      url += "/api/home";
      break;
    case "API_REPORT_TASK" :
      url += "/api/automation/report";
      break;
    case "PERFORMANCE_REPORT_TASK" :
      url += "/performance/report/all";
      break;
    case "PERFORMANCE_TEST_TASK" :
      url += "/performance/test/edit/" + d.resourceId;
      break;
    case "TRACK_TEST_CASE_TASK" :
      url += "/track/case/all?resourceId=" + d.resourceId;
      break;
    case "TRACK_HOME_TASK" :
      url += "/track/home";
      break;
    case "TRACK_REPORT_TASK" :
      url += "/track/testPlan/reportList";
      break;
    default:
      break;
  }
  return url;
}

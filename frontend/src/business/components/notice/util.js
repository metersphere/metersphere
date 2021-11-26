export function getOperation(operation) {
  switch (operation) {
    case "CREATE":
      operation = "创建了";
      break;
    case "UPDATE":
      operation = "更新了";
      break;
    case "DELETE":
      operation = "删除了";
      break;
    case "COMMENT":
      operation = "评论了";
      break;
    case "COMPLETE":
      operation = "完成了";
      break;
    case "CLOSE_SCHEDULE":
      operation = "关闭了定时任务";
      break;
    case "CASE_CREATE":
      operation = "创建了接口用例";
      break;
    case "CASE_UPDATE":
      operation = "更新了接口用例";
      break;
    case "CASE_DELETE":
      operation = "删除了接口用例";
      break;
    case "EXECUTE_SUCCESSFUL":
    case "EXECUTE_FAILED":
    case "EXECUTE_COMPLETED":
      operation = "执行";
      break;
    default:
      break;
  }
  return operation;
}

export function getResource(d) {
  switch (d.operation) {
    case "CASE_CREATE":
    case "CASE_UPDATE":
    case "CASE_DELETE":
      return "";
  }

  let resourceType = "";
  switch (d.resourceType) {
    case "JENKINS_TASK" :
      resourceType = "Jenkins";
      if (d.operation === 'EXECUTE_SUCCESSFUL') {
        resourceType = "Jenkins 成功";
      }
      if (d.operation === 'EXECUTE_FAILED') {
        resourceType = "Jenkins 失败";
      }
      if (d.operation === 'EXECUTE_COMPLETED') {
        resourceType = "Jenkins 完成";
      }
      break;
    case "TEST_PLAN_TASK" :
      resourceType = "测试计划";
      break;
    case "REVIEW_TASK" :
      resourceType = "测试评审";
      break;
    case "DEFECT_TASK" :
      resourceType = "缺陷";
      break;
    case "SWAGGER_TASK" :
      resourceType = "Swagger";
      break;
    case "API_AUTOMATION_TASK" :
      resourceType = "接口自动化";
      if (d.operation === 'EXECUTE_SUCCESSFUL') {
        resourceType = "接口自动化成功";
      }
      if (d.operation === 'EXECUTE_FAILED') {
        resourceType = "接口自动化失败";
      }
      break;
    case "API_DEFINITION_TASK" :
      resourceType = "接口定义";
      if (d.operation === 'EXECUTE_SUCCESSFUL') {
        resourceType = "接口用例成功";
      }
      if (d.operation === 'EXECUTE_FAILED') {
        resourceType = "接口用例失败";
      }
      break;
    case "API_HOME_TASK" :
      resourceType = "接口测试首页";
      break;
    case "API_REPORT_TASK" :
      resourceType = "接口测试报告";
      break;
    case "PERFORMANCE_REPORT_TASK" :
      resourceType = "性能测试报告";
      break;
    case "PERFORMANCE_TEST_TASK" :
      resourceType = "性能测试";
      if (d.operation === 'EXECUTE_COMPLETED') {
        resourceType = "性能测试完成";
      }
      break;
    case "TRACK_TEST_CASE_TASK" :
      resourceType = "测试用例";
      break;
    case "TRACK_HOME_TASK" :
      resourceType = "测试跟踪首页";
      break;
    case "TRACK_REPORT_TASK" :
      resourceType = "测试跟踪报告";
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

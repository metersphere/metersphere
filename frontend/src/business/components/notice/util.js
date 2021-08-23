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
      break;
    case "API_DEFINITION_TASK" :
      resourceType = "接口定义";
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

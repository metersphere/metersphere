// 模板展示字段icon
export enum TaskCenterEnum {
  API_CASE = 'API_CASE', // 接口用例
  API_SCENARIO = 'API_SCENARIO', // 接口场景
  UI_TEST = 'UI_TEST', // ui测试
  LOAD_TEST = 'LOAD_TEST', // 性能测试
  TEST_PLAN = 'TEST_PLAN', // 测试计划
  TEST_RESOURCE = 'TEST_RESOURCE', // 测试资源
  API_IMPORT = 'API_IMPORT', // API导入
}

// 执行方式
export enum ExecutionMethods {
  SCHEDULE = 'SCHEDULE', // 定时任务
  MANUAL = 'MANUAL', // 手动执行
  API = 'API', // 接口调用
  // BATCH = 'API', // 批量执行
}

export enum ExecutionMethodsLabel {
  SCHEDULE = 'project.taskCenter.scheduledTask', // 定时任务
  MANUAL = 'project.taskCenter.manualExecution', // 手动执行
  API = 'project.taskCenter.interfaceCall', // 接口调用
  BATCH = 'project.taskCenter.batchExecution', // 批量执行
}
export default {};

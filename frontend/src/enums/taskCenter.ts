// 任务中心 tab  枚举
export enum TaskCenterEnum {
  CASE = 'CASE',
  DETAIL = 'DETAIL',
  BACKEND = 'BACKEND',
  API_IMPORT = 'API_IMPORT',
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

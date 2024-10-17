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

export enum ExecuteStatusEnum {
  PENDING = 'PENDING', // 待执行
  RUNNING = 'RUNNING', // 执行中
  COMPLETED = 'COMPLETED', // 执行完成
  RERUNNING = 'RERUNNING', // 失败重跑
  STOPPED = 'STOPPED', // 执行中止
}

export enum ExecuteResultEnum {
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR',
  FAKE_ERROR = 'FAKE_ERROR',
}

export enum ExecuteTriggerMode {
  MANUAL = 'MANUAL',
  BATCH = 'BATCH',
  API = 'API',
  SCHEDULE = 'SCHEDULE',
}

export enum ExecuteTaskType {
  API_IMPORT = 'API_IMPORT',
  API_SCENARIO = 'API_SCENARIO',
  BUG_SYNC = 'BUG_SYNC',
  DEMAND_SYNC = 'DEMAND_SYNC',
  TEST_PLAN = 'TEST_PLAN',
}

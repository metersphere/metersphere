export enum testPlanTypeEnum {
  ALL = 'ALL',
  TEST_PLAN = 'TEST_PLAN',
  GROUP = 'GROUP',
}

export enum RunMode {
  SERIAL = 'SERIAL', // 串行
  PARALLEL = 'PARALLEL', // 并行
}

// 功能：FUNCTIONAL_CASE/接口定义：API/接口用例：API_CASE/场景：SCENARIO_CASE
export enum PlanMinderAssociateType {
  FUNCTIONAL_CASE = 'FUNCTIONAL',
  API = 'API',
  API_CASE = 'API_CASE',
  SCENARIO_CASE = 'API_SCENARIO',
}
// 测试集类型(功能：FUNCTIONAL/接口用例：API/场景：SCENARIO)
export enum PlanMinderCollectionType {
  FUNCTIONAL = 'FUNCTIONAL',
  API = 'API',
  SCENARIO = 'SCENARIO',
}
export enum TestPlanStatusEnum {
  PREPARED = 'PREPARED', // 未开始
  UNDERWAY = 'UNDERWAY', // 进行中
  COMPLETED = 'COMPLETED', // 已完成
  ARCHIVED = 'ARCHIVED', // 已归档
}

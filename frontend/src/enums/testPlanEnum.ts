export enum testPlanTypeEnum {
  ALL = 'ALL',
  TEST_PLAN = 'TEST_PLAN',
  GROUP = 'GROUP',
}

export enum RunMode {
  SERIAL = 'SERIAL', // 串行
  PARALLEL = 'PARALLEL', // 并行
}

export enum TestSetType {
  FUNCTIONAL_CASE = 'FUNCTIONAL_CASE',
  API_CASE = 'API_CASE',
  SCENARIO_CASE = 'SCENARIO_CASE',
}

export enum FailRetry {
  STEP = 'STEP',
  SCENARIO = 'SCENARIO',
}

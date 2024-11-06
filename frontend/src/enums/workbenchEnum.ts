export enum WorkOverviewIconEnum {
  FUNCTIONAL = 'icon-icon_functional_testing1',
  CASE_REVIEW = 'icon-icon_people',
  API = 'icon-icon_api',
  API_CASE = 'icon-icon_case',
  API_SCENARIO = 'icon-icon_environment',
  API_PLAN = 'icon-icon_calendar_dot',
  BUG_COUNT = 'icon-icon_bug',
}

export enum WorkOverviewEnum {
  FUNCTIONAL = 'FUNCTIONAL', // 功能用例
  CASE_REVIEW = 'CASE_REVIEW', // 用例评审
  API = 'API',
  API_CASE = 'API_CASE',
  API_SCENARIO = 'API_SCENARIO',
  API_PLAN = 'API_PLAN', // 接口计划
  BUG_COUNT = 'BUG_COUNT', // 缺陷数量
}

export enum WorkCardEnum {
  /**
   * default
   */
  CREATE_BY_ME = 'CREATE_BY_ME', // 我创建的
  PROJECT_VIEW = 'PROJECT_VIEW', // 项目概览
  PROJECT_MEMBER_VIEW = 'PROJECT_MEMBER_VIEW', // 项目成员概览

  /**
   * functional
   */
  CASE_COUNT = 'CASE_COUNT', // 用例数量统计
  ASSOCIATE_CASE_COUNT = 'ASSOCIATE_CASE_COUNT', // 关联用例统计
  REVIEW_CASE_COUNT = 'REVIEW_CASE_COUNT', // 用例评审数量统计
  REVIEWING_BY_ME = 'REVIEWING_BY_ME', // 待我评审

  /**
   * api
   */
  API_COUNT = 'API_COUNT', // 接口数量统计
  API_CASE_COUNT = 'API_CASE_COUNT', // 接口用例数量统计
  SCENARIO_COUNT = 'SCENARIO_COUNT', // 场景用例数量统计
  API_CHANGE = 'API_CHANGE', // 接口变更统计

  /**
   * test_plan
   */
  TEST_PLAN_COUNT = 'TEST_PLAN_COUNT', // 测试计划数量统计
  PLAN_LEGACY_BUG = 'PLAN_LEGACY_BUG', // 计划遗留bug统计

  /**
   * bug
   */
  BUG_COUNT = 'BUG_COUNT', // 缺陷数量统计
  CREATE_BUG_BY_ME = 'CREATE_BUG_BY_ME', // 我创建的缺陷
  HANDLE_BUG_BY_ME = 'HANDLE_BUG_BY_ME', // 待我处理的缺陷
  BUG_HANDLE_USER = 'BUG_HANDLE_USER', // 缺陷处理人统计
}

export enum FeatureEnum {
  TEST_PLAN = 'TEST_PLAN',
  TEST_CASE = 'TEST_CASE',
  CASE_REVIEW = 'CASE_REVIEW',
  API_CASE = 'API_CASE',
  API_SCENARIO = 'API_SCENARIO',
  BUG = 'BUG',
}
